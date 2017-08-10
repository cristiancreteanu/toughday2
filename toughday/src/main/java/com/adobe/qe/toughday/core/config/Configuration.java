package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.Main;
import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.core.config.parsers.cli.CliParser;
import com.adobe.qe.toughday.core.config.parsers.yaml.YamlParser;
import com.adobe.qe.toughday.core.engine.RunMode;
import com.adobe.qe.toughday.core.engine.PublishMode;
import com.adobe.qe.toughday.publishers.CSVPublisher;
import com.adobe.qe.toughday.publishers.ConsolePublisher;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * An object that has all that configurations parsed and objects instantiated.
 */
public class Configuration {
    private static final Logger LOGGER =  LogManager.getLogger(Configuration.class);

    private static final String DEFAULT_RUN_MODE = "normal";
    private static final String DEFAULT_PUBLISH_MODE = "simple";

    private List<ClassLoader> classLoaders;
    private GlobalArgs globalArgs;
    private TestSuite suite;
    private RunMode runMode;
    private PublishMode publishMode;
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

    private void processJarFile(JarFile jarFile, String pathToJarFile) throws MalformedURLException {
        Enumeration<JarEntry> jarContent = jarFile.entries();
        URL[] urls = {new URL("jar:file:" + pathToJarFile + "!/")};
        URLClassLoader classLoader = URLClassLoader.newInstance(urls, Main.class.getClassLoader());
        classLoaders.add(classLoader);

        while (jarContent.hasMoreElements()) {
            JarEntry jarEntry = jarContent.nextElement();
            if (jarEntry.isDirectory() || !(jarEntry.getName().endsWith(".class"))) {
                continue;
            }

            String className = jarEntry.getName().replace(".class","");
            className = className.replaceAll("/",".");
            System.out.println("class name : " + className);
            try {
                //SequentialTestBase test = (SequentialTestBase) classLoader.loadClass(className).newInstance();
                //test.test();
                classLoader.loadClass(className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigParams configParams = collectConfigurations(cmdLineArgs);

        Map<String, String> globalArgsMeta = configParams.getGlobalParams();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);
        applyLogLevel(globalArgs.getLogLevel());

        classLoaders = new ArrayList<>();
        if (globalArgs.extensions.compareTo("") != 0) {
            String[] jarFilesPaths = globalArgs.getExtensions().split(",");
            JarFile jarFile = null;

            for (String pathToJarFile : jarFilesPaths) {
                try {
                    jarFile = new JarFile(pathToJarFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    processJarFile(jarFile, pathToJarFile);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        }

        this.runMode = getRunMode(configParams);
        this.publishMode = getPublishMode(configParams);

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

    private RunMode getRunMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, String> runModeParams = configParams.getRunModeParams();
        if(runModeParams.size() != 0 && !runModeParams.containsKey("type")) {
            throw new IllegalStateException("The Run mode doesn't have a type");
        }


        String type = runModeParams.size() != 0 ? runModeParams.get("type") : DEFAULT_RUN_MODE;
        Class<? extends RunMode> runModeClass = ReflectionsContainer.getInstance().getRunModeClasses().get(type);

        if(runModeClass == null) {
            throw new IllegalStateException("A run mode with type \"" + type + "\" does not exist");
        }

        return  createObject(runModeClass,  runModeParams);
    }

    private PublishMode getPublishMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, String> publishModeParams = configParams.getPublishModeParams();
        if(publishModeParams.size() != 0 && !publishModeParams.containsKey("type")) {
            throw new IllegalStateException("The Publish mode doesn't have a type");
        }

        String type = publishModeParams.size() != 0 ? publishModeParams.get("type") : DEFAULT_PUBLISH_MODE;
        Class<? extends PublishMode> publishModeClass = ReflectionsContainer.getInstance().getPublishModeClasses().get(type);

        if(publishModeClass == null) {
            throw new IllegalStateException("A publish mode with type \"" + type + "\" does not exist");
        }

        return  createObject(publishModeClass,  publishModeParams);
    }

    private void applyLogLevel(Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.setLevel(level);
        }
        System.setProperty("toughday.log.level", level.name());
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
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
     * Getter for the run mode
     * @return
     */
    public RunMode getRunMode() {
        return runMode;
    }

    /**
     * Getter for the publish mode
     * @return
     */
    public PublishMode getPublishMode() {
        return publishMode;
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
        private long duration;
        private Map<String, Publisher> publishers;
        private long timeout;
        private String protocol;
        private String extensions;

        public static final String DEFAULT_DURATION = "1d";
        public static final String DEFAULT_USER = "admin";
        public static final String DEFAULT_PASSWORD = "admin";
        public static final String DEFAULT_PORT_STRING = "4502";
        public static final String DEFAULT_PROTOCOL = "http";
        public static final String DEFAULT_EXTENSIONS = "";
        public static final int DEFAULT_PORT = Integer.parseInt(DEFAULT_PORT_STRING);

        public static final String DEFAULT_TIMEOUT_STRING = "180"; // 3 minutes
        public static final long DEFAULT_TIMEOUT = 3 * 60 * 1000l; // 5 minutes

        public static final String DEFAULT_LOG_LEVEL_STRING = "INFO";
        public static final Level DEFAULT_LOG_LEVEL = Level.valueOf(DEFAULT_LOG_LEVEL_STRING);

        public static final String DEFAULT_DRY_RUN = "false";

        private boolean installSampleContent = true;
        private String contextPath;
        private Level logLevel = DEFAULT_LOG_LEVEL;
        private boolean dryRun = Boolean.parseBoolean(DEFAULT_DRY_RUN);

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
            this.protocol = DEFAULT_PROTOCOL;
            this.extensions = DEFAULT_EXTENSIONS;
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

        @ConfigArgSet(required = false, desc = "How long the tests will run. Can be expressed in s(econds), m(inutes), h(ours), and/or d(ays). Example: 1h30m.", defaultValue = DEFAULT_DURATION, order = 6)
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        @ConfigArgSet(required = false, desc ="How long a test will run before it will be interrupted and marked as failed. Expressed in seconds",
                defaultValue = DEFAULT_TIMEOUT_STRING, order = 7)
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

        @ConfigArgSet(required = false, desc = "What type of protocol to use for the host", defaultValue = DEFAULT_PROTOCOL)
        public void setProtocol(String protocol) { this.protocol = protocol; }

        @ConfigArgSet(required = false, desc = "Install ToughDay 2 Sample Content.", defaultValue = "true")
        public void setInstallSampleContent(String installSampleContent) {
            this.installSampleContent = Boolean.valueOf(installSampleContent);
        }

        @ConfigArgSet(required = false, desc = "Context path.")
        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_LOG_LEVEL_STRING, desc = "Log level for ToughDay Engine")
        public void setLogLevel(String logLevel) {
            this.logLevel = Level.valueOf(logLevel);
        }

        @ConfigArgSet(required = false, defaultValue = "false", desc = "If true, prints the resulting configuration and does not run any tests.")
        public void setDryRun(String dryRun) {
            this.dryRun = Boolean.valueOf(dryRun);
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_EXTENSIONS, desc = "Jar file to be loaded.")
        public void setExtensions(String extensions) {
            this.extensions = extensions;
        }


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
        public Level getLogLevel() {
            return logLevel;
        }

        @ConfigArgGet
        public boolean getDryRun() {
            return dryRun;
        }

        @ConfigArgGet
        public String getExtensions() {
            return extensions;
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

        /**
         * Parses a duration specified as string and converts it to seconds.
         * @param duration a duration in d(ays), h(ours), m(inutes), s(econds). Ex. 1d12h30m30s
         * @return number of seconds for the respective duration.
         */
        public static long parseDurationToSeconds(String duration) {
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
