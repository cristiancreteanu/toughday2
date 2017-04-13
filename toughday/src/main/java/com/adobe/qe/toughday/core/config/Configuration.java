package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.core.config.parsers.cli.CliParser;
import com.adobe.qe.toughday.core.config.parsers.yaml.YamlParser;
import com.adobe.qe.toughday.publishers.CSVPublisher;
import com.adobe.qe.toughday.publishers.ConsolePublisher;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * An object that has all that configurations parsed and objects instantiated.
 */
public class Configuration {
    private static final Logger LOGGER =  LogManager.getLogger(Configuration.class);


    private GlobalArgs globalArgs;
    private TestSuite suite;
    PredefinedSuites predefinedSuites = new PredefinedSuites();

    private TestSuite getTestSuite(Map<String, String> globalArgsMeta)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!globalArgsMeta.containsKey("suite"))
            return createObject(TestSuite.class, globalArgsMeta);

        /* TODO allow multiple predefined test suites.
         What happens with the setup step if two or more suites have setup steps? */

        TestSuite testSuite = new TestSuite();
        String[] testSuiteNames = globalArgsMeta.remove("suite").split(",");
        for (String testSuiteName : testSuiteNames) {
            if (!predefinedSuites.containsKey(testSuiteName)) {
                throw new IllegalArgumentException("Unknown suite: " + testSuiteName);
            }
            testSuite.addAll(predefinedSuites.get(testSuiteName));
        }
        return testSuite;
    }

    private void checkInvalidArgs(Map<String, String> args, List<String>... whitelisted) {
        Map<String, String> argsCopy = new HashMap<>();
        argsCopy.putAll(args);
        args = argsCopy;

        for(int i = 0; i < whitelisted.length; i++) {
            List<String> whitelist = whitelisted[i];
            for(String whitelistedArg : whitelist) {
                args.remove(whitelistedArg);
            }
        }

        if(args.size() == 0) return;

        for (String key : args.keySet()) {
            LOGGER.error("Invalid property \"" + key +"\"");
        }

        throw new IllegalStateException("There are invalid properties in the configuration. Please check thoughday.log.");
    }


    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigParams configParams = collectConfigurations(cmdLineArgs);

        Map<String, String> globalArgsMeta = configParams.getGlobalParams();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);

        // Add a default publishers if none is specified
        if (configParams.getPublishers().size() == 0) {
            configParams.addPublisher(ConsolePublisher.class.getSimpleName(), new HashMap<String, String>());
            configParams.addPublisher(CSVPublisher.class.getSimpleName(), new HashMap<String, String>() {{ put("append", "true"); }});
        }

        for(ConfigParams.ClassMetaObject publisherMeta : configParams.getPublishers()) {
            Publisher publisher = createObject(
                    ReflectionsContainer.getInstance().getPublisherClasses().get(publisherMeta.getClassName()),
                    publisherMeta.getParameters());

            checkInvalidArgs(publisherMeta.getParameters());
            this.globalArgs.addPublisher(publisher);
        }

        suite = getTestSuite(globalArgsMeta);

        if ( (suite.getTests().size() == 0) && (configParams.getTestsToAdd().size() == 0) ) {
            // Replace the empty suite with the default predefined suite if no test has been configured,
            // either by selecting a suite or manually using --add
            this.suite = predefinedSuites.getDefaultSuite();
        }

        for (ConfigParams.ClassMetaObject testMeta : configParams.getTestsToAdd()) {
            AbstractTest test = createObject(
                    ReflectionsContainer.getInstance().getTestClasses().get(testMeta.getClassName()),
                    testMeta.getParameters());

            // defaults
            int weight = (testMeta.getParameters().containsKey("weight"))
                    ? Integer.parseInt(testMeta.getParameters().remove("weight")) : 1;
            long timeout = (testMeta.getParameters().containsKey("timeout"))
                    ? Integer.parseInt(testMeta.getParameters().remove("timeout")) : -1;
            long counter = (testMeta.getParameters().containsKey("count"))
                    ? Integer.parseInt(testMeta.getParameters().remove("count")) : -1;

            suite.add(test, weight, timeout, counter);
            checkInvalidArgs(testMeta.getParameters());
        }


        // Add and configure tests to the suite
        for(ConfigParams.NamedMetaObject itemMeta : configParams.getItemsToConfig()) {
            if (suite.contains(itemMeta.getName())) {
                AbstractTest testObject = suite.getTest(itemMeta.getName());
                setObjectProperties(testObject, itemMeta.getParameters());
                if (itemMeta.getParameters().containsKey("weight")) {
                    suite.replaceWeight(itemMeta.getName(), Integer.parseInt(itemMeta.getParameters().remove("weight")));
                }
                if (itemMeta.getParameters().containsKey("timeout")) {
                    suite.replaceTimeout(itemMeta.getName(), Integer.parseInt(itemMeta.getParameters().remove("timeout")));
                }
                if (itemMeta.getParameters().containsKey("count")) {
                    suite.replaceCount(itemMeta.getName(), Integer.parseInt(itemMeta.getParameters().remove("count")));
                }
            } else if (globalArgs.containsPublisher(itemMeta.getName())) {
                Publisher publisherObject = globalArgs.getPublisher(itemMeta.getName());
                setObjectProperties(publisherObject, itemMeta.getParameters());
            } else {
                throw new IllegalStateException("No test or publisher found with name \"" + itemMeta.getName() + "\", so we can't configure it.");
            }

            checkInvalidArgs(itemMeta.getParameters());
        }

        // Exclude tests and publishers
        for (String itemName : configParams.getItemsToExclude()) {
            if(suite.contains(itemName)) {
                suite.remove(itemName);
            } else if (globalArgs.containsPublisher(itemName)) {
                globalArgs.removePublisher(itemName);
            } else  {
                throw new IllegalStateException("No test or publisher found with name \"" + itemName + "\", so we can't exclude it.");
            }
        }

        checkInvalidArgs(globalArgsMeta, CliParser.parserArgs);
        for (AbstractTest test : suite.getTests()) {
            test.setGlobalArgs(this.globalArgs);
        }
    }

    private ConfigParams collectConfigurations(String[] cmdLineArgs) {
        ConfigParams configs = new YamlParser().parse(cmdLineArgs);
        configs.merge(new CliParser().parse(cmdLineArgs));
        return configs;
    }

    /**
     * Getter for the predefined suites
     * @return
     */
    public HashMap<String, TestSuite> getPredefinedSuites(){
        return predefinedSuites;
    }

    /**
     * Getter for the suite
     * @return
     */
    public TestSuite getTestSuite() {
        return suite;
    }

    /**
     * Getter for the global args
     * @return
     */
    public GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    /**
     * Method for getting the property from a setter method
     * @param methodName
     * @return
     */
    public static String propertyFromMethod(String methodName) {
        return methodName.startsWith("set") || methodName.startsWith("get") ? StringUtils.lowerCase(methodName.substring(3)) : StringUtils.lowerCase(methodName);
    }

    /**
     * Method for setting an object properties annotated with ConfigArgSet using reflection
     * @param object
     * @param args
     * @param <T>
     * @return the object with the properties set
     * @throws InvocationTargetException caused by reflection
     * @throws IllegalAccessException caused by reflection
     */
    public static <T> T setObjectProperties(T object, Map<String, String> args) throws InvocationTargetException, IllegalAccessException {
        Class classObject = object.getClass();
        LOGGER.info("Configuring object of class: " + classObject.getSimpleName());
        for (Method method : classObject.getMethods()) {
            ConfigArgSet annotation = method.getAnnotation(ConfigArgSet.class);
            if (annotation == null) {
                continue;
            }

            String property = propertyFromMethod(method.getName());
            Object value = args.remove(property);
            if (value == null) {
                if (annotation.required()) {
                    throw new IllegalArgumentException("Property \"" + property + "\" is required for class " + classObject.getSimpleName());
                }
                else {
                    //will use default value
                    continue;
                }
            }

            LOGGER.info("\tSetting property \"" + property + "\" to: \"" + value + "\"");
            //TODO fix this ugly thing: all maps should be String -> String, but snake yaml automatically converts Integers, etc. so for now we call toString.
            method.invoke(object, value.toString());
        }
        return object;
    }

    /**
     * Method for creating and configuring an object using reflection
     * @param classObject
     * @param args
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public static <T> T createObject(Class<? extends T> classObject, Map<String, String> args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Constructor constructor = null;
        try {
            constructor = classObject.getConstructor(null);
        } catch (NoSuchMethodException e) {
            NoSuchMethodException explicitException = new NoSuchMethodException(classObject.getSimpleName()
                    + " class must have a constructor without arguments");
            explicitException.initCause(e);
            throw explicitException;
        }

        T object = (T) constructor.newInstance();
        setObjectProperties(object, args);
        return object;
    }

    /**
     * Method for getting the parser for the configuration.
     * @param args
     * @return
     */
    private ConfigurationParser getConfigurationParser(String[] args) {
        //TODO Insert logic here to select from other types of parsers
        return new CliParser();
    }

    /**
     * Class for global arguments.
     */
    public static class GlobalArgs {
        private String host;
        private int port;
        private String user;
        private String password;
        private int concurrency;
        private long waitTime;
        private long duration;
        private Map<String, Publisher> publishers;
        private long timeout;
        private String protocol;

        public static final String DEFAULT_DURATION = "1d";
        public static final String DEFAULT_USER = "admin";
        public static final String DEFAULT_PASSWORD = "admin";
        public static final String DEFAULT_PORT_STRING = "4502";
        public static final String DEFAULT_PROTOCOL = "http";
        public static final int DEFAULT_PORT = Integer.parseInt(DEFAULT_PORT_STRING);

        public static final String DEFAULT_TIMEOUT_STRING = "180"; // 3 minutes
        public static final long DEFAULT_TIMEOUT = 3 * 60 * 1000l; // 5 minutes

        public static final String DEFAULT_CONCURRENCY_STRING = "200";
        public static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_STRING);

        public static final String DEFAULT_WAIT_TIME_STRING = "300";
        public static final long DEFAULT_WAIT_TIME = Long.parseLong(DEFAULT_WAIT_TIME_STRING);
        private boolean installSampleContent = true;
        private String contextPath;
        private String runMode = "normal";
        private String publishMode = "simple";
        private long interval = 5;
        private int load = 50;

        /**
         * Constructor
         */
        public GlobalArgs() {
            this.publishers = new HashMap<>();
            this.port = DEFAULT_PORT;
            this.user = DEFAULT_USER;
            this.password = DEFAULT_PASSWORD;
            this.duration = parseDurationToSeconds(DEFAULT_DURATION);
            this.timeout = DEFAULT_TIMEOUT;
            this.waitTime = DEFAULT_WAIT_TIME;
            this.concurrency = DEFAULT_CONCURRENCY;
            this.protocol = DEFAULT_PROTOCOL;
        }

        // Global config args

        @ConfigArgSet(required = true, desc = "The host name/ip which will be targeted", order = 1)
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArgSet(required = false, desc = "The port of the host", defaultValue = DEFAULT_PORT_STRING, order = 2)
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArgSet(required = false, desc = "User name for the instance", defaultValue = DEFAULT_USER, order = 3)
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArgSet(required = false, desc = "Password for the given user", defaultValue = DEFAULT_PASSWORD, order = 4)
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArgSet(required = false, desc = "The number of concurrent threads that Tough Day will use", defaultValue = DEFAULT_CONCURRENCY_STRING, order = 5)
        public void setConcurrency(String concurrencyString) {
            this.concurrency = Integer.parseInt(concurrencyString);
        }

        @ConfigArgSet(required = false, desc = "How long the tests will run. Can be expressed in s(econds), m(inutes), h(ours), and/or d(ays). Example: 1h30m.", defaultValue = DEFAULT_DURATION, order = 6)
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArgSet(required = false, desc = "The wait time between two consecutive test runs for a specific thread. Expressed in milliseconds",
                defaultValue = DEFAULT_WAIT_TIME_STRING, order = 7)
        public void setWaitTime(String waitTime) {
            this.waitTime = Integer.parseInt(waitTime);
        }

        @ConfigArgSet(required = false, desc ="How long a test will run before it will be interrupted and marked as failed. Expressed in seconds",
                defaultValue = DEFAULT_TIMEOUT_STRING, order = 7)
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

        @ConfigArgSet(required = false, desc = "What type of protocol to use for the host", defaultValue = DEFAULT_PROTOCOL)
        public void setProtocol(String protocol) { this.protocol = protocol; }

        @ConfigArgSet(required = false, desc = "Run mode for test execution", defaultValue = "normal")
        public void setRunMode(String runMode) {
            this.runMode = runMode;
        }

        @ConfigArgSet(required = false, desc = "Install ToughDay 2 Sample Content.", defaultValue = "true")
        public void setInstallSampleContent(String installSampleContent) {
            this.installSampleContent = Boolean.valueOf(installSampleContent);
        }

        @ConfigArgSet(required = false, desc = "Context path.")
        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        @ConfigArgSet(required = false, desc = "Publish mode for outputing results", defaultValue = "simple")
        public void setPublishMode(String publishMode) { this.publishMode = publishMode; }

        @ConfigArgSet(required = false, defaultValue = "5s", desc = "Set the publishing interval. Can be expressed in s(econds), m(inutes), h(ours). Example: 1m30s. (Available only when publishmode=intervals)")
        public void setInterval(String interval) { this.interval = parseDurationToSeconds(interval); }

        @ConfigArgSet(required = false, defaultValue = "50", desc = "Set the load, in requests per second for the \"constantload\" runmode.  (Available only when runmode=constantload)")
        public void setLoad(String load) { this.load = Integer.parseInt(load); }

        // Adders and getters

        public void addPublisher(Publisher publisher) {
            if (publishers.containsKey(publisher.getName())) {
                throw new IllegalStateException("There is already a publisher named \"" + publisher.getName() + "\"." +
                        "Please provide a different name using the \"name\" property.");
            }
            publishers.put(publisher.getName(), publisher);
        }

        public Publisher getPublisher(String publisherName) {
            if (!publishers.containsKey(publisherName)) {
                throw new IllegalStateException("Could not find a publisher with the name \"" + publisherName + "\" to configure it.");
            }
            return publishers.get(publisherName);
        }

        public boolean containsPublisher(String publisherName) {
            return publishers.containsKey(publisherName);
        }

        public void removePublisher(String publisherName) {
            Publisher publisher = publishers.remove(publisherName);
            if (publisher == null) {
                throw new IllegalStateException("Could not exclude publisher \"" + publisherName + "\", because there was no publisher configured with that name");
            }
        }

        @ConfigArgGet
        public String getRunMode() {
            return runMode;
        }

        @ConfigArgGet
        public int getConcurrency() {
            return concurrency;
        }

        @ConfigArgGet
        public long getWaitTime() {
            return waitTime;
        }

        @ConfigArgGet
        public long getDuration() {
            return duration;
        }

        public Collection<Publisher> getPublishers() {
            return publishers.values();
        }

        @ConfigArgGet
        public long getTimeout() {
            return timeout;
        }

        @ConfigArgGet
        public String getHost() {
            return host;
        }

        @ConfigArgGet
        public int getPort() {
            return port;
        }

        @ConfigArgGet
        public String getUser() {
            return user;
        }

        @ConfigArgGet
        public String getPassword() {
            return password;
        }

        @ConfigArgGet
        public String getProtocol() { return protocol; }

        @ConfigArgGet
        public boolean getInstallSampleContent() {
            return installSampleContent;
        }

        @ConfigArgGet
        public String getContextPath() { return this.contextPath; }

        @ConfigArgGet
        public String getPublishMode() { return this.publishMode; }

        @ConfigArgGet
        public long getInterval() { return this.interval; }

        @ConfigArgGet
        public int getLoad() { return this.load; }


        // Helper methods

        private static long unitToSeconds(char unit) {
            long factor = 1;
            // there are no breaks after case, so unitToSeconds('d') will return 1 * 24 * 60 * 60 * 1
            switch (unit) {
                case 'd': factor *= 24;
                case 'h': factor *= 60;
                case 'm': factor *= 60;
                case 's': factor *= 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown duration unit: " + unit);
            }
            return factor;
        }

        private static long parseDurationToSeconds(String duration) {
            long finalDuration = 0l;
            long intermDuration = 0l;

            for (char c : duration.toCharArray()) {
                if (Character.isDigit(c)) {
                    intermDuration = intermDuration * 10 + (long) (c - '0');
                } else {
                    finalDuration += intermDuration * unitToSeconds(c);
                    intermDuration = 0;
                }
                // everything else, like whitespaces is ignored
            }
            return finalDuration;
        }
    }
}
