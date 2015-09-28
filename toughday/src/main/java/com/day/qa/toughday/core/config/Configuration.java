package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.Publisher;
import com.day.qa.toughday.core.ReflectionsContainer;
import com.day.qa.toughday.core.TestSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 18/09/15.
 * An object that has all that configurations parsed and objects instantiated.
 */
public class Configuration {
    private GlobalArgs globalArgs;
    private TestSuite suite;
    PredefinedSuites predefinedSuites = new PredefinedSuites();

    private TestSuite getTestSuite(HashMap<String, String> globalArgsMeta)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(!globalArgsMeta.containsKey("Suite"))
            return createObject(TestSuite.class, globalArgsMeta);

        /* TODO allow multiple predefined test suites.
         What happens with the setup step if two or more suites have setup steps? */
        String testSuiteName = globalArgsMeta.get("Suite");
        if(!predefinedSuites.containsKey(testSuiteName)) {
            throw new IllegalArgumentException("Unknown suite: " + testSuiteName);
        }
        return predefinedSuites.get(testSuiteName);
    }

    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigurationParser parser = getConfigurationParser(cmdLineArgs);
        ConfigParams configParams= parser.parse(cmdLineArgs);

        HashMap<String, String> globalArgsMeta = configParams.getGlobalParams();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);

        for(ConfigParams.ClassMetaObject publisherMeta : configParams.getPublishers()) {
            Publisher publisher = createObject(
                    ReflectionsContainer.getInstance().getPublisherClasses().get(publisherMeta.getClassName()),
                    publisherMeta.getParameters());
            this.globalArgs.addPublisher(publisher);
        }

        suite = getTestSuite(globalArgsMeta);

        for(String testName : configParams.getTestsToExclude()) {
            suite.remove(testName);
        }

        for(ConfigParams.NamedMetaObject testMeta : configParams.getTestsToConfig()) {
            AbstractTest testObject = suite.getTest(testMeta.getName());
            setObjectProperties(testObject, testMeta.getParameters());
            if (testMeta.getParameters().containsKey("Timeout")) {
                suite.replaceTimeout(testMeta.getName(), Integer.parseInt(testMeta.getParameters().get("Timeout")));
            }
            if (testMeta.getParameters().containsKey("Weight")) {
                    suite.replaceWeight(testMeta.getName(), Integer.parseInt(testMeta.getParameters().get("Weight")));
            }
        }

        for(ConfigParams.ClassMetaObject testMeta : configParams.getTestsToAdd()) {
            String testName = testMeta.getParameters().get("Name");
            if(suite.contains(testName)) {
                throw new IllegalArgumentException("Suite already contains a test named: " + testName);
            }

            AbstractTest test = createObject(
                    ReflectionsContainer.getInstance().getTestClasses().get(testMeta.getClassName()),
                    testMeta.getParameters());
            if(!testMeta.getParameters().containsKey("Weight"))
                throw new IllegalArgumentException("Property Weight is required for class " + test.getClass().getSimpleName());

            if(!testMeta.getParameters().containsKey("Timeout")) {
                suite.add(test, Integer.parseInt(testMeta.getParameters().get("Weight")));
            } else {
                suite.add(test, Integer.parseInt(testMeta.getParameters().get("Weight")),
                        Integer.parseInt(testMeta.getParameters().get("Timeout")));
            }
        }

        for(AbstractTest test : suite.getTests()) {
            test.setGlobalArgs(this.globalArgs);
        }

        if(suite.getTests().size() == 0)
            throw new IllegalStateException("No tests added to the suite.");

        if(globalArgs.getPublishers().size() == 0)
            throw new IllegalStateException("No publishers added.");
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
        return methodName.startsWith("set") ? methodName.substring(3) : methodName;
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
    public static <T> T setObjectProperties(T object, HashMap<String, String> args) throws InvocationTargetException, IllegalAccessException {
        Class classObject = object.getClass();
        for(Method method : classObject.getMethods()) {
            ConfigArg annotation = method.getAnnotation(ConfigArg.class);
            if(annotation == null) {
                continue;
            }
            String property = propertyFromMethod(method.getName());
            String value = args.get(property);
            if(value == null) {
                if(annotation.required()) {
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
    public static <T> T createObject(Class<? extends T> classObject, HashMap<String, String> args)
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
     * Created by tuicu on 07/09/15.
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

        private static final String DEFAULT_USER = "admin";
        private static final String DEFAULT_PASSWORD = "admin";
        private static final int DEFAULT_PORT = 4502;
        private static final long DEFAULT_TIMEOUT = 300000; //5minutes
        private static final int DEFAULT_CONCURRENCY = 1;
        private static final long DEFAULT_WAIT_TIME = 10;

        /**
         * Constructor
         */
        public GlobalArgs() {
            this.publishers = new ArrayList<>();
            this.port = DEFAULT_PORT;
            this.user = DEFAULT_USER;
            this.password = DEFAULT_PASSWORD;
            this.timeout = DEFAULT_TIMEOUT;
            this.waitTime = DEFAULT_WAIT_TIME;
            this.concurrency = DEFAULT_CONCURRENCY;
        }

        private static long unitToSeconds(char unit) {
            long factor = 1;
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
            long intermDuration = 0;

            for(char c : duration.toCharArray()) {
                if(Character.isDigit(c)) {
                    intermDuration = intermDuration * 10 + (long) (c - '0');
                } else {
                    finalDuration += intermDuration * unitToSeconds(c);
                    intermDuration = 0;
                }
            }
            return finalDuration;
        }

        @ConfigArg
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArg(required = false)
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArg(required = false)
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArg(required = false)
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArg(required = false, desc = "number of concurrent users")
        public void setConcurrency(String concurrencyString) {
            this.concurrency = Integer.parseInt(concurrencyString);
        }

        @ConfigArg(desc = "how long will toughday run")
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArg(required = false, desc = "wait time between two consecutive test runs for a user in milliseconds")
        public void setWaitTime(String waitTime) {
            this.waitTime = Integer.parseInt(waitTime);
        }

        @ConfigArg(required = false, desc ="how long can a test run before it is interrupted and marked as failed")
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

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
    }
}
