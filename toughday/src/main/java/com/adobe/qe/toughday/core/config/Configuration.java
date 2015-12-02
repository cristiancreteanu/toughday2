package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.publishers.ConsolePublisher;
import org.apache.commons.lang.StringUtils;

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
    private GlobalArgs globalArgs;
    private TestSuite suite;
    PredefinedSuites predefinedSuites = new PredefinedSuites();

    private TestSuite getTestSuite(Map<String, String> globalArgsMeta)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!globalArgsMeta.containsKey("suite"))
            return createObject(TestSuite.class, globalArgsMeta);

        /* TODO allow multiple predefined test suites.
         What happens with the setup step if two or more suites have setup steps? */
        String testSuiteName = globalArgsMeta.get("suite");
        if (!predefinedSuites.containsKey(testSuiteName)) {
            throw new IllegalArgumentException("Unknown suite: " + testSuiteName);
        }
        return predefinedSuites.get(testSuiteName);
    }

    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        ConfigurationParser parser = getConfigurationParser(cmdLineArgs);
        ConfigParams configParams = parser.parse(cmdLineArgs);

        Map<String, String> globalArgsMeta = configParams.getGlobalParams();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);

        // Add a default publisher if none is specified
        if (configParams.getPublishers().size() == 0) {
            configParams.addPublisher(ConsolePublisher.class.getSimpleName(), new HashMap<String, String>() {{ put("Clear", "true"); }});
        }

        for(ConfigParams.ClassMetaObject publisherMeta : configParams.getPublishers()) {
            Publisher publisher = createObject(
                    ReflectionsContainer.getInstance().getPublisherClasses().get(publisherMeta.getClassName()),
                    publisherMeta.getParameters());
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
            if (testMeta.getParameters().containsKey("timeout")) {
                suite.replaceTimeout(testMeta.getName(), Integer.parseInt(testMeta.getParameters().get("timeout")));
            }
            if (testMeta.getParameters().containsKey("weight")) {
                    suite.replaceWeight(testMeta.getName(), Integer.parseInt(testMeta.getParameters().get("weight")));
            }
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
                    ? Integer.parseInt(testMeta.getParameters().get("weight")) : 1;
            long timeout = (testMeta.getParameters().containsKey("timeout"))
                    ? Integer.parseInt(testMeta.getParameters().get("timeout")) : -1;
            long counter = (testMeta.getParameters().containsKey("count"))
                    ? Integer.parseInt(testMeta.getParameters().get("count")) : -1;

            suite.add(test, weight, timeout, counter);
        }

        for (AbstractTest test : suite.getTests()) {
            test.setGlobalArgs(this.globalArgs);
        }
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
        return methodName.startsWith("set") ? StringUtils.lowerCase(methodName.substring(3)) : methodName;
    }

    /**
     * Method for setting an object properties annotated with ConfigArg using reflection
     * @param object
     * @param args
     * @param <T>
     * @return the object with the properties set
     * @throws InvocationTargetException caused by reflection
     * @throws IllegalAccessException caused by reflection
     */
    public static <T> T setObjectProperties(T object, Map<String, String> args) throws InvocationTargetException, IllegalAccessException {
        Class classObject = object.getClass();
        for (Method method : classObject.getMethods()) {
            ConfigArg annotation = method.getAnnotation(ConfigArg.class);
            if (annotation == null) {
                continue;
            }

            String property = propertyFromMethod(method.getName());
            String value = args.get(property);
            if (value == null) {
                if (annotation.required()) {
                    throw new IllegalArgumentException("Property " + property + " is required for class " + classObject.getSimpleName());
                }
                else {
                    //will use default value
                    continue;
                }
            }
            method.invoke(object, value);
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
        private List<Publisher> publishers;
        private long timeout;

        public static final String DEFAULT_DURATION = "1d";
        public static final String DEFAULT_USER = "admin";
        public static final String DEFAULT_PASSWORD = "admin";
        public static final String DEFAULT_HOST = "localhost";
        public static final String DEFAULT_PORT_STRING = "4502";
        public static final int DEFAULT_PORT = Integer.parseInt(DEFAULT_PORT_STRING);

        public static final String DEFAULT_TIMEOUT_STRING = "5m"; // 5 minutes
        public static final long DEFAULT_TIMEOUT = 5 * 60 * 1000l; // 5 minutes

        public static final String DEFAULT_CONCURRENCY_STRING = "30";
        public static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_STRING);

        public static final String DEFAULT_WAIT_TIME_STRING = "1000";
        public static final long DEFAULT_WAIT_TIME = Long.parseLong(DEFAULT_WAIT_TIME_STRING);

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
        }

        // Global config args

        @ConfigArg(required = false, defaultValue = DEFAULT_HOST, order = 1)
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArg(required = false, defaultValue = DEFAULT_PORT_STRING, order = 2)
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArg(required = false, defaultValue = DEFAULT_USER, order = 3)
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArg(required = false, defaultValue = DEFAULT_PASSWORD, order = 4)
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArg(required = false, desc = "Number of concurrent users", defaultValue = DEFAULT_CONCURRENCY_STRING, order = 5)
        public void setConcurrency(String concurrencyString) {
            this.concurrency = Integer.parseInt(concurrencyString);
        }

        @ConfigArg(required = false, desc = "How long to run toughday", defaultValue = DEFAULT_DURATION, order = 6)
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArg(required = false, desc = "wait time between two consecutive test runs for a user in milliseconds",
                defaultValue = DEFAULT_WAIT_TIME_STRING, order = 7)
        public void setWaitTime(String waitTime) {
            this.waitTime = Integer.parseInt(waitTime);
        }

        @ConfigArg(required = false, desc ="How long a test runs before it is interrupted and marked as failed",
                defaultValue = DEFAULT_TIMEOUT_STRING, order = 7)
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }


        // Adders and getters

        public void addPublisher(Publisher publisher) {
            publishers.add(publisher);
        }

        public int getConcurrency() {
            return concurrency;
        }

        public long getWaitTime() {
            return waitTime;
        }

        public long getDuration() {
            return duration;
        }

        public List<Publisher> getPublishers() {
            return publishers;
        }

        public long getTimeout() {
            return timeout;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }


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
