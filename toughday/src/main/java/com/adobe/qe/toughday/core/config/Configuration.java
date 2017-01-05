package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.core.config.parsers.cli.CliParser;
import com.adobe.qe.toughday.core.config.parsers.yaml.YamlParser;
import com.adobe.qe.toughday.publishers.ConsolePublisher;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        String testSuiteName = globalArgsMeta.remove("suite");
        if (!predefinedSuites.containsKey(testSuiteName)) {
            throw new IllegalArgumentException("Unknown suite: " + testSuiteName);
        }
        return predefinedSuites.get(testSuiteName);
    }

    private void checkInvalidArgs(Map<String, String> args) {
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

        // Add a default publisher if none is specified
        if (configParams.getPublishers().size() == 0) {
            configParams.addPublisher(ConsolePublisher.class.getSimpleName(), new HashMap<String, String>());
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

        // Exclude tests
        for (String testName : configParams.getTestsToExclude()) {
            suite.remove(testName);
        }

        // Add and configure tests to the suite
        for(ConfigParams.NamedMetaObject testMeta : configParams.getTestsToConfig()) {
            AbstractTest testObject = suite.getTest(testMeta.getName());
            setObjectProperties(testObject, testMeta.getParameters());
            if (testMeta.getParameters().containsKey("weight")) {
                suite.replaceWeight(testMeta.getName(), Integer.parseInt(testMeta.getParameters().remove("weight")));
            }
            if (testMeta.getParameters().containsKey("timeout")) {
                suite.replaceTimeout(testMeta.getName(), Integer.parseInt(testMeta.getParameters().remove("timeout")));
            }
            if (testMeta.getParameters().containsKey("count")) {
                suite.replaceCount(testMeta.getName(), Integer.parseInt(testMeta.getParameters().remove("count")));
            }

            checkInvalidArgs(testMeta.getParameters());
        }

        for (ConfigParams.ClassMetaObject testMeta : configParams.getTestsToAdd()) {
            String testName = testMeta.getParameters().get("name");
            if (suite.contains(testName)) {
                throw new IllegalArgumentException("Suite already contains a test named: " + testName);
            }

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

        checkInvalidArgs(globalArgsMeta);
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
                    throw new IllegalArgumentException("Property " + property + " is required for class " + classObject.getSimpleName());
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

    public interface RunMode {
        String value();
        String description();
    }

    //TODO when implementing constant troughput refactor into individual classes.
    public enum RUN_MODE implements RunMode {
        DRY {
            @Override
            public String value() {
                return DRY_VALUE;
            }

            @Override
            public String description() {
                return "Prints the resulting configuration. Does not run any test.";
            }
        }, NORMAL {
            @Override
            public String value() {
                return NORMAL_VALUE;
            }
            @Override
            public String description() {
                return "Runs tests normally.";
            }
        };

        public static RUN_MODE fromString(String runModeString) {
            for (RUN_MODE runMode : RUN_MODE.values()) {
                if(runMode.value().equals(runModeString)) {
                    return runMode;
                }
            }

            throw new IllegalArgumentException("Invalid run mode \"" + runModeString + "\"");
        }

        public static final String DRY_VALUE = "dry";
        public static final String NORMAL_VALUE = "normal";
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
        private List<Publisher> publishers;
        private long timeout;
        private String protocol;

        public static final String DEFAULT_DURATION = "1d";
        public static final String DEFAULT_USER = "admin";
        public static final String DEFAULT_PASSWORD = "admin";
        public static final String DEFAULT_HOST = "localhost";
        public static final String DEFAULT_PORT_STRING = "4502";
        public static final String DEFAULT_PROTOCOL = "http";
        public static final int DEFAULT_PORT = Integer.parseInt(DEFAULT_PORT_STRING);

        public static final String DEFAULT_TIMEOUT_STRING = "3m"; // 3 minutes
        public static final long DEFAULT_TIMEOUT = 3 * 60 * 1000l; // 5 minutes

        public static final String DEFAULT_CONCURRENCY_STRING = "30";
        public static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_STRING);

        public static final String DEFAULT_WAIT_TIME_STRING = "1000";
        public static final long DEFAULT_WAIT_TIME = Long.parseLong(DEFAULT_WAIT_TIME_STRING);
        private RUN_MODE runMode = RUN_MODE.NORMAL;

        /**
         * Constructor
         */
        public GlobalArgs() {
            this.publishers = new ArrayList<>();
            this.host = DEFAULT_HOST;
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

        @ConfigArgSet(required = false, defaultValue = DEFAULT_HOST, order = 1)
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_PORT_STRING, order = 2)
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_USER, order = 3)
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_PASSWORD, order = 4)
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArgSet(required = false, desc = "Number of concurrent users", defaultValue = DEFAULT_CONCURRENCY_STRING, order = 5)
        public void setConcurrency(String concurrencyString) {
            this.concurrency = Integer.parseInt(concurrencyString);
        }

        @ConfigArgSet(required = false, desc = "How long to run toughday", defaultValue = DEFAULT_DURATION, order = 6)
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArgSet(required = false, desc = "wait time between two consecutive test runs for a user in milliseconds",
                defaultValue = DEFAULT_WAIT_TIME_STRING, order = 7)
        public void setWaitTime(String waitTime) {
            this.waitTime = Integer.parseInt(waitTime);
        }

        @ConfigArgSet(required = false, desc ="How long a test runs before it is interrupted and marked as failed",
                defaultValue = DEFAULT_TIMEOUT_STRING, order = 7)
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

        @ConfigArgSet(required = false, desc = "What protocol to use", defaultValue = DEFAULT_PROTOCOL)
        public void setProtocol(String protocol) { this.protocol = protocol; }

        @ConfigArgSet(required = false, desc = "Run mode for test execution", defaultValue = RUN_MODE.NORMAL_VALUE)
        public void setRunMode(String runMode) {
            this.runMode = RUN_MODE.fromString(runMode);
        }

        // Adders and getters

        public void addPublisher(Publisher publisher) {
            publishers.add(publisher);
        }

        @ConfigArgGet
        public String getRunMode() {
            return runMode.value();
        }

        public RUN_MODE getRunModeEnum() {
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

        public List<Publisher> getPublishers() {
            return publishers;
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
