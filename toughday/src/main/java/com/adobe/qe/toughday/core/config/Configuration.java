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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.reflections.Reflections;

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
    private static final Logger LOGGER = LogManager.getLogger(Configuration.class);

    private static final String DEFAULT_RUN_MODE = "normal";
    private static final String DEFAULT_PUBLISH_MODE = "simple";
    public static boolean newClassesAlert = false;
    PredefinedSuites predefinedSuites = new PredefinedSuites();
    private GlobalArgs globalArgs;
    private TestSuite suite;
    private RunMode runMode;
    private PublishMode publishMode;
    private List<ClassLoader> classLoaders;

    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigParams configParams = collectConfigurations(cmdLineArgs);

        Map<String, String> globalArgsMeta = configParams.getGlobalParams();
        List<JarFile> jarFiles = new ArrayList<>();
        classLoaders = new ArrayList<>();

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);
        applyLogLevel(globalArgs.getLogLevel());

        if (globalArgs.extensions.compareTo("") != 0) {
            String[] jarFilesPaths = globalArgs.getExtensions().split(",");
            for (String pathToJarFile : jarFilesPaths) {
                try {
                    JarFile jarFile = new JarFile(pathToJarFile);
                    jarFiles.add(jarFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            processJarFiles(jarFiles, formJarURLs(Arrays.asList(globalArgs.extensions.split(","))));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Reflections reflections = new Reflections(classLoaders);
        ReflectionsContainer.getInstance().merge(reflections);

        this.runMode = getRunMode(configParams);
        this.publishMode = getPublishMode(configParams);
        suite = getTestSuite(globalArgsMeta);

        boolean foundAPublisher = false;
        boolean foundATest = false;

        // add tests and publishers
        for (ConfigParams.ClassMetaObject itemToAdd : configParams.getItemsToAdd()) {
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(itemToAdd.getClassName())) {
                foundATest = true;
                AbstractTest test = createObject(
                        ReflectionsContainer.getInstance().getTestClasses().get(itemToAdd.getClassName()),
                        itemToAdd.getParameters());

                // defaults
                int weight = (itemToAdd.getParameters().containsKey("weight"))
                        ? Integer.parseInt(itemToAdd.getParameters().remove("weight")) : 1;
                long timeout = (itemToAdd.getParameters().containsKey("timeout"))
                        ? Integer.parseInt(itemToAdd.getParameters().remove("timeout")) : -1;
                long counter = (itemToAdd.getParameters().containsKey("count"))
                        ? Integer.parseInt(itemToAdd.getParameters().remove("count")) : -1;

                suite.add(test, weight, timeout, counter);
                checkInvalidArgs(itemToAdd.getParameters());
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(itemToAdd.getClassName())) {
                foundAPublisher = true;
                Publisher publisher = createObject(
                        ReflectionsContainer.getInstance().getPublisherClasses().get(itemToAdd.getClassName()),
                        itemToAdd.getParameters());

                checkInvalidArgs(itemToAdd.getParameters());
                this.globalArgs.addPublisher(publisher);
            } else {
                throw new IllegalArgumentException("Unknown publisher or test class: " + itemToAdd.getClassName());
            }
        }

        // Add a default publishers if none is specified
        if (!foundAPublisher) {
            Publisher publisher = createObject(ConsolePublisher.class, new HashMap<String, String>());
            this.globalArgs.addPublisher(publisher);
            publisher = createObject(CSVPublisher.class, new HashMap<String, String>() {{
                put("append", "true");
            }});
            this.globalArgs.addPublisher(publisher);
        }

        // Add a default suite of tests if no test is added or no predefined suite is choosen.
        if ((suite.getTests().size() == 0) && (!foundATest)) {
            // Replace the empty suite with the default predefined suite if no test has been configured,
            // either by selecting a suite or manually using --add
            this.suite = predefinedSuites.getDefaultSuite();
        }

        // Add and configure tests to the suite
        for (ConfigParams.NamedMetaObject itemMeta : configParams.getItemsToConfig()) {
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
            if (suite.contains(itemName)) {
                suite.remove(itemName);
            } else if (globalArgs.containsPublisher(itemName)) {
                globalArgs.removePublisher(itemName);
            } else {
                throw new IllegalStateException("No test or publisher found with name \"" + itemName + "\", so we can't exclude it.");
            }
        }

        if (!CliParser.helpRequired) {
            checkInvalidArgs(globalArgsMeta, CliParser.parserArgs);
        }
        for (AbstractTest test : suite.getTests()) {
            test.setGlobalArgs(this.globalArgs);
        }
    }

    /**
     * Method for getting the property from a setter method
     *
     * @param methodName
     * @return
     */
    public static String propertyFromMethod(String methodName) {
        return methodName.startsWith("set") || methodName.startsWith("get") ? StringUtils.lowerCase(methodName.substring(3)) : StringUtils.lowerCase(methodName);
    }

    /**
     * Method for setting an object properties annotated with ConfigArgSet using reflection
     *
     * @param object
     * @param args
     * @param <T>
     * @return the object with the properties set
     * @throws InvocationTargetException caused by reflection
     * @throws IllegalAccessException    caused by reflection
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
                if (annotation.required() && !CliParser.helpRequired) {
                    throw new IllegalArgumentException("Property \"" + property + "\" is required for class " + classObject.getSimpleName());
                } else {
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
     *
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

        for (int i = 0; i < whitelisted.length; i++) {
            List<String> whitelist = whitelisted[i];
            for (String whitelistedArg : whitelist) {
                args.remove(whitelistedArg);
            }
        }

        if (args.size() == 0) return;

        for (String key : args.keySet()) {
            LOGGER.error("Invalid property \"" + key + "\"");
        }

        throw new IllegalStateException("There are invalid properties in the configuration. Please check thoughday.log.");
    }

    private URL[] formJarURLs(List<String> pathsToJars) {
        List<URL> urls = new ArrayList<>();
        for (String path : pathsToJars) {
            try {
                urls.add(new URL("jar:file:" + path + "!/"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return urls.toArray(new URL[0]);
    }

    private void processJarFiles(List<JarFile> jarFiles, URL[] urls) throws MalformedURLException {
        URLClassLoader classLoader = URLClassLoader.newInstance(urls, Main.class.getClassLoader());
        Map<String, String> newClasses = new HashMap<>();
        classLoaders.add(classLoader);

        for (JarFile jar : jarFiles) {
            Enumeration<JarEntry> jarContent = jar.entries();
            while (jarContent.hasMoreElements()) {
                JarEntry jarEntry = jarContent.nextElement();
                if (jarEntry.isDirectory() || !(jarEntry.getName().endsWith(".class"))) {
                    continue;
                }
                String className = jarEntry.getName().replace(".class", "");
                className = className.replaceAll("/", ".");

                if (newClasses.containsKey(className)) {
                    throw new IllegalStateException("A class named " + className + " already exists in the jar file named " + newClasses.get(className));
                } else if (ReflectionsContainer.getInstance().containsClass(className)) {
                    throw new IllegalStateException("A class named " + className + " already exists in toughday default package.");
                } else {
                    newClasses.put(className, jar.getName());
                    newClassesAlert = true;
                }

                try {
                    classLoader.loadClass(className);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private RunMode getRunMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, String> runModeParams = configParams.getRunModeParams();
        if (runModeParams.size() != 0 && !runModeParams.containsKey("type")) {
            throw new IllegalStateException("The Run mode doesn't have a type");
        }


        String type = runModeParams.size() != 0 ? runModeParams.get("type") : DEFAULT_RUN_MODE;
        Class<? extends RunMode> runModeClass = ReflectionsContainer.getInstance().getRunModeClasses().get(type);

        if (runModeClass == null) {
            throw new IllegalStateException("A run mode with type \"" + type + "\" does not exist");
        }

        return createObject(runModeClass, runModeParams);
    }

    private PublishMode getPublishMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, String> publishModeParams = configParams.getPublishModeParams();
        if (publishModeParams.size() != 0 && !publishModeParams.containsKey("type")) {
            throw new IllegalStateException("The Publish mode doesn't have a type");
        }

        String type = publishModeParams.size() != 0 ? publishModeParams.get("type") : DEFAULT_PUBLISH_MODE;
        Class<? extends PublishMode> publishModeClass = ReflectionsContainer.getInstance().getPublishModeClasses().get(type);

        if (publishModeClass == null) {
            throw new IllegalStateException("A publish mode with type \"" + type + "\" does not exist");
        }

        return createObject(publishModeClass, publishModeParams);
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
     *
     * @return
     */
    public HashMap<String, TestSuite> getPredefinedSuites() {
        return predefinedSuites;
    }

    /**
     * Getter for the suite
     *
     * @return
     */
    public TestSuite getTestSuite() {
        return suite;
    }

    /**
     * Getter for the global args
     *
     * @return
     */
    public GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    /**
     * Getter for the run mode
     *
     * @return
     */
    public RunMode getRunMode() {
        return runMode;
    }

    /**
     * Getter for the publish mode
     *
     * @return
     */
    public PublishMode getPublishMode() {
        return publishMode;
    }

    /**
     * Method for getting the parser for the configuration.
     *
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
        private String host;
        private int port;
        private String user;
        private String password;
        private long duration;
        private Map<String, Publisher> publishers;
        private long timeout;
        private String protocol;
        private String extensions;
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

        private static long unitToSeconds(char unit) {
            long factor = 1;
            // there are no breaks after case, so unitToSeconds('d') will return 1 * 24 * 60 * 60 * 1
            switch (unit) {
                case 'd':
                    factor *= 24;
                case 'h':
                    factor *= 60;
                case 'm':
                    factor *= 60;
                case 's':
                    factor *= 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown duration unit: " + unit);
            }
            return factor;
        }

        /**
         * Parses a duration specified as string and converts it to seconds.
         *
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

        @ConfigArgSet(required = false, desc = "How long the tests will run. Can be expressed in s(econds), m(inutes), h(ours), and/or d(ays). Example: 1h30m.", defaultValue = DEFAULT_DURATION, order = 6)
        public void setDuration(String durationString) {
            this.duration = parseDurationToSeconds(durationString);
        }

        public Collection<Publisher> getPublishers() {
            return publishers.values();
        }

        @ConfigArgGet
        public long getTimeout() {
            return timeout;
        }

        @ConfigArgSet(required = false, desc = "How long a test will run before it will be interrupted and marked as failed. Expressed in seconds",
                defaultValue = DEFAULT_TIMEOUT_STRING, order = 7)
        public void setTimeout(String timeout) {
            this.timeout = Integer.parseInt(timeout) * 1000;
        }

        @ConfigArgGet
        public String getHost() {
            return host;
        }


        // Adders and getters

        @ConfigArgSet(required = true, desc = "The host name/ip which will be targeted", order = 1)
        public void setHost(String host) {
            this.host = host;
        }

        @ConfigArgGet
        public int getPort() {
            return port;
        }

        @ConfigArgSet(required = false, desc = "The port of the host", defaultValue = DEFAULT_PORT_STRING, order = 2)
        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        @ConfigArgGet
        public String getUser() {
            return user;
        }

        @ConfigArgSet(required = false, desc = "User name for the instance", defaultValue = DEFAULT_USER, order = 3)
        public void setUser(String user) {
            this.user = user;
        }

        @ConfigArgGet
        public String getPassword() {
            return password;
        }

        @ConfigArgSet(required = false, desc = "Password for the given user", defaultValue = DEFAULT_PASSWORD, order = 4)
        public void setPassword(String password) {
            this.password = password;
        }

        @ConfigArgGet
        public String getProtocol() {
            return protocol;
        }

        @ConfigArgSet(required = false, desc = "What type of protocol to use for the host", defaultValue = DEFAULT_PROTOCOL)
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        @ConfigArgGet
        public boolean getInstallSampleContent() {
            return installSampleContent;
        }

        @ConfigArgSet(required = false, desc = "Install ToughDay 2 Sample Content.", defaultValue = "true")
        public void setInstallSampleContent(String installSampleContent) {
            this.installSampleContent = Boolean.valueOf(installSampleContent);
        }

        @ConfigArgGet
        public String getContextPath() {
            return this.contextPath;
        }

        @ConfigArgSet(required = false, desc = "Context path.")
        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        @ConfigArgGet
        public Level getLogLevel() {
            return logLevel;
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_LOG_LEVEL_STRING, desc = "Log level for ToughDay Engine")
        public void setLogLevel(String logLevel) {
            this.logLevel = Level.valueOf(logLevel);
        }

        @ConfigArgGet
        public boolean getDryRun() {
            return dryRun;
        }

        @ConfigArgSet(required = false, defaultValue = "false", desc = "If true, prints the resulting configuration and does not run any tests.")
        public void setDryRun(String dryRun) {
            this.dryRun = Boolean.valueOf(dryRun);
        }

        // Helper methods

        @ConfigArgGet
        public String getExtensions() {
            return extensions;
        }

        @ConfigArgSet(required = false, defaultValue = DEFAULT_EXTENSIONS, desc = "Jar files to be loaded.")
        public void setExtensions(String extensions) {
            this.extensions = extensions;
        }
    }
}
