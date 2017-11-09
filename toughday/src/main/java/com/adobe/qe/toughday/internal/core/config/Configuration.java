package com.adobe.qe.toughday.internal.core.config;

import com.adobe.qe.toughday.Main;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.Publisher;
import com.adobe.qe.toughday.internal.core.config.parsers.yaml.GenerateYamlConfiguration;
import com.adobe.qe.toughday.internal.core.ReflectionsContainer;
import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.config.parsers.cli.CliParser;
import com.adobe.qe.toughday.internal.core.config.parsers.yaml.YamlParser;
import com.adobe.qe.toughday.internal.core.engine.PublishMode;
import com.adobe.qe.toughday.internal.core.engine.RunMode;
import com.adobe.qe.toughday.internal.core.metrics.Metric;
import com.adobe.qe.toughday.internal.core.metrics.Name;
import com.adobe.qe.toughday.internal.core.metrics.Timestamp;
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
    PredefinedSuites predefinedSuites = new PredefinedSuites();
    private GlobalArgs globalArgs;
    private TestSuite suite;
    private RunMode runMode;
    private PublishMode publishMode;

    private void handleExtensions(ConfigParams configParams) {

        List<String> extensionList = new ArrayList<>();
        List<ConfigParams.ClassMetaObject> itemsToAddCopy = new ArrayList<>(configParams.getItemsToAdd());
        //List<String> itemsToExcludeCopy = new ArrayList<>(configParams.getItemsToExclude());

        // look for extension jar files that should be loaded.
        for (ConfigParams.ClassMetaObject itemToAdd : itemsToAddCopy) {
            if (itemToAdd.getClassName().endsWith(".jar")) {
                configParams.getItemsToAdd().remove(itemToAdd);
                extensionList.add(itemToAdd.getClassName());
            }
        }

        if (extensionList.isEmpty()) {
            return;
        }
        // look for extension jar files that should be excluded.
        /*for (String itemToExclude : itemsToExcludeCopy) {
            if (itemToExclude.endsWith(".jar")) {
                configParams.getItemsToExclude().remove(itemToExclude);
                if (!extensionList.contains(itemToExclude)) {
                    throw new IllegalStateException("No extension found with name \"" + itemToExclude + "\", so we can't exclude it.");
                }
                extensionList.remove(itemToExclude);
            }
        }*/

        List<JarFile> jarFiles = createJarFiles(extensionList);
        URLClassLoader classLoader = null;
        try {
            classLoader = processJarFiles(jarFiles, formJarURLs(extensionList));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Reflections reflections = new Reflections(classLoader);

        // make reflection container aware of the new dynamically loaded classes
        ReflectionsContainer.getInstance().merge(reflections);
    }

    /**
     *  Creates a jar file for each extension file that should be loaded.
     * @param extensionList A list of names representing the jar files that should be loaded.
     */
    private List<JarFile> createJarFiles(List<String> extensionList) {
        List<JarFile> jarFiles = new ArrayList<>();
        for (String extensionFileName : extensionList) {
            try {
                JarFile jarFile = new JarFile(extensionFileName);
                jarFiles.add(jarFile);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to find " + extensionFileName + " file.");
            }
        }

        return jarFiles;
    }

    /**
     *  Creates an URL for each jar file, using its filename.
     * @param extensionsFileNames
     * @return
     */
    private URL[] formJarURLs(List<String> extensionsFileNames) {
        List<URL> urls = new ArrayList<>();
        for (String filename : extensionsFileNames) {
            try {
                urls.add(new URL("jar:file:" + filename + "!/"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return urls.toArray(new URL[0]);
    }

    // loads all classes from the extension jar files using a new class loader.

    private URLClassLoader processJarFiles(List<JarFile> jarFiles, URL[] urls) throws MalformedURLException {
        URLClassLoader classLoader = URLClassLoader.newInstance(urls, Main.class.getClassLoader());
        Map<String, String> newClasses = new HashMap<>();

        for (JarFile jar : jarFiles) {
            Enumeration<JarEntry> jarContent = jar.entries();
            while (jarContent.hasMoreElements()) {
                JarEntry jarEntry = jarContent.nextElement();
                if (jarEntry.isDirectory() || !(jarEntry.getName().endsWith(".class"))) {
                    continue;
                }
                String className = jarEntry.getName().replace(".class", "");
                className = className.replaceAll("/", ".");

                // check if a class with this name already exists
                if (newClasses.containsKey(className)) {
                    throw new IllegalStateException("A class named " + className + " already exists in the jar file named " + newClasses.get(className));
                } else if (ReflectionsContainer.getInstance().containsClass(className)) {
                    throw new IllegalStateException("A class named " + className + " already exists in toughday default package.");
                } else {
                    newClasses.put(className, jar.getName());
                }

                // load class
                try {
                    classLoader.loadClass(className);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return classLoader;
    }


    public Configuration(String[] cmdLineArgs)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ConfigParams configParams = collectConfigurations(cmdLineArgs);
        ConfigParams copyOfConfigParams = ConfigParams.deepClone(configParams);
        Map<String, Class> items = new HashMap<>();

        // we should load extensions before any configuration object is created.
        handleExtensions(configParams);

        Map<String, Object> globalArgsMeta = configParams.getGlobalParams();
        for (String helpOption : CliParser.availableHelpOptions) {
           if (globalArgsMeta.containsKey(helpOption)) {
               return;
           }
        }

        this.globalArgs = createObject(GlobalArgs.class, globalArgsMeta);
        applyLogLevel(globalArgs.getLogLevel());

        this.runMode = getRunMode(configParams);
        this.publishMode = getPublishMode(configParams);
        suite = getTestSuite(globalArgsMeta);

        for (AbstractTest abstractTest : suite.getTests()) {
            items.put(abstractTest.getName(), abstractTest.getClass());
        }

        // add tests,publishers and metrics
        for (ConfigParams.ClassMetaObject itemToAdd : configParams.getItemsToAdd()) {
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(itemToAdd.getClassName())) {
                AbstractTest test = createObject(ReflectionsContainer.getInstance().getTestClasses().get(itemToAdd.getClassName()), itemToAdd.getParameters());
                items.put(test.getName(), test.getClass());

                // defaults
                int weight = (itemToAdd.getParameters().containsKey("weight"))
                        ? (Integer) (itemToAdd.getParameters().remove("weight")) : 1;
                long timeout = (itemToAdd.getParameters().containsKey("timeout"))
                        ? (Integer) (itemToAdd.getParameters().remove("timeout")) : -1;
                long counter = (itemToAdd.getParameters().containsKey("count"))
                        ? (Integer) (itemToAdd.getParameters().remove("count")) : -1;

                suite.add(test, weight, timeout, counter);
                checkInvalidArgs(itemToAdd.getParameters());
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(itemToAdd.getClassName())) {
                Publisher publisher = createObject(
                        ReflectionsContainer.getInstance().getPublisherClasses().get(itemToAdd.getClassName()),
                        itemToAdd.getParameters());
                items.put(publisher.getName(), publisher.getClass());

                checkInvalidArgs(itemToAdd.getParameters());
                this.globalArgs.addPublisher(publisher);
            } else if (ReflectionsContainer.getInstance().getMetricClasses().containsKey(itemToAdd.getClassName())) {

                Metric metric = createObject(ReflectionsContainer.getInstance().getMetricClasses().get(itemToAdd.getClassName()),
                        itemToAdd.getParameters());
                items.put(metric.getName(), metric.getClass());

                checkInvalidArgs(itemToAdd.getParameters());
                this.globalArgs.addMetric(metric);
            } else if (itemToAdd.getClassName().equals("BASICMetrics")) {
                Collection<Metric> basicMetrics = Metric.basicMetrics;
                for (Metric metric : basicMetrics) {
                    this.globalArgs.addMetric(metric);
                    items.put(metric.getName(), metric.getClass());
                }
            } else if (itemToAdd.getClassName().equals("DEFAULTMetrics")) {
                Collection<Metric> defaultMetrics = Metric.defaultMetrics;
                for (Metric metric : defaultMetrics) {
                    this.globalArgs.addMetric(metric);
                    items.put(metric.getName(), metric.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unknown publisher, test or metric class: " + itemToAdd.getClassName());
            }
        }

        // Add default publishers if none is specified
        if (globalArgs.getPublishers().size() == 0) {
            Publisher publisher = createObject(ConsolePublisher.class, new HashMap<String, Object>());
            this.globalArgs.addPublisher(publisher);
            publisher = createObject(CSVPublisher.class, new HashMap<String, Object>() {{
                put("append", "true");
            }});
            this.globalArgs.addPublisher(publisher);
        }

        // Add a default suite of tests if no test is added or no predefined suite is choosen.
        if (suite.getTests().size() == 0) {
            // Replace the empty suite with the default predefined suite if no test has been configured,
            // either by selecting a suite or manually using --add
            this.suite = predefinedSuites.getDefaultSuite();
        }

        // Add default metrics if no metric is specified.
        if (globalArgs.metrics.size() == 0) {
            Collection<Metric> defaultMetrics = Metric.defaultMetrics;
            for (Metric metric : defaultMetrics) {
                this.globalArgs.addMetric(metric);
            }
        }

        // Add and configure tests to the suite
        for (ConfigParams.NamedMetaObject itemMeta : configParams.getItemsToConfig()) {
            if (suite.contains(itemMeta.getName())) {
                AbstractTest testObject = suite.getTest(itemMeta.getName());
                if ( itemMeta.getParameters().containsKey("name")) {
                    suite.replaceName(testObject, String.valueOf(itemMeta.getParameters().remove("name")));
                }
                setObjectProperties(testObject, itemMeta.getParameters());
                items.put(testObject.getName(), testObject.getClass());
                if (itemMeta.getParameters().containsKey("weight")) {
                    suite.replaceWeight(testObject.getName(), (Integer) (itemMeta.getParameters().remove("weight")));
                }
                if (itemMeta.getParameters().containsKey("timeout")) {
                    suite.replaceTimeout(testObject.getName(), (Integer) (itemMeta.getParameters().remove("timeout")));
                }
                if (itemMeta.getParameters().containsKey("count")) {
                    suite.replaceCount(testObject.getName(), (Integer) (itemMeta.getParameters().remove("count")));
                }

            } else if (globalArgs.containsPublisher(itemMeta.getName())) {
                Publisher publisherObject = globalArgs.getPublisher(itemMeta.getName());
                String name = publisherObject.getName();
                setObjectProperties(publisherObject, itemMeta.getParameters());
                if (!name.equals(publisherObject.getName())) {
                    this.getGlobalArgs().updatePublisherName(name, publisherObject.getName());
                }
            } else if (globalArgs.containsMetric(itemMeta.getName())) {
                Metric metricObject = globalArgs.getMetric(itemMeta.getName());
                String name = metricObject.getName();
                setObjectProperties(metricObject, itemMeta.getParameters());
                if (!name.equals(metricObject.getName())) {
                    this.getGlobalArgs().updateMetricName(name, metricObject.getName());
                }
            } else {
                throw new IllegalStateException("No test/publisher/metric found with name \"" + itemMeta.getName() + "\", so we can't configure it.");
            }

            checkInvalidArgs(itemMeta.getParameters());
        }

        // Exclude tests/publishers/metrics
        for (String itemName : configParams.getItemsToExclude()) {
            if (suite.contains(itemName)) {
                suite.remove(itemName);
            } else if (globalArgs.containsPublisher(itemName)) {
                globalArgs.removePublisher(itemName);
            } else if (getGlobalArgs().containsMetric(itemName)) {
                globalArgs.removeMetric(itemName);
            } else {
                throw new IllegalStateException("No test/publisher/metric found with name \"" + itemName + "\", so we can't exclude it.");
            }
        }

        checkInvalidArgs(globalArgsMeta, CliParser.parserArgs);
        for (AbstractTest test : suite.getTests()) {
            test.setGlobalArgs(this.globalArgs);
        }

        // Check if we should create a configuration file for this run.
        if (this.getGlobalArgs().getSaveConfig()) {
            GenerateYamlConfiguration generateYaml = new GenerateYamlConfiguration(copyOfConfigParams, items);
            generateYaml.createYamlConfigurationFile();
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
    public static <T> T setObjectProperties(T object, Map<String, Object> args) throws InvocationTargetException, IllegalAccessException {
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
                } else {
                    String defaultValue = annotation.defaultValue();
                    if (defaultValue.compareTo("") != 0) {
                        method.invoke(object, defaultValue);
                    }
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
    public static <T> T createObject(Class<? extends T> classObject, Map<String, Object> args)
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

    private TestSuite getTestSuite(Map<String, Object> globalArgsMeta)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!globalArgsMeta.containsKey("suite"))
            return createObject(TestSuite.class, globalArgsMeta);

        /* TODO allow multiple predefined test suites.
         What happens with the setup step if two or more suites have setup steps? */

        TestSuite testSuite = new TestSuite();
        String[] testSuiteNames = String.valueOf(globalArgsMeta.remove("suite")).split(",");
        for (String testSuiteName : testSuiteNames) {
            if (!predefinedSuites.containsKey(testSuiteName)) {
                throw new IllegalArgumentException("Unknown suite: " + testSuiteName);
            }
            testSuite.addAll(predefinedSuites.get(testSuiteName));
        }
        return testSuite;
    }

    private void checkInvalidArgs(Map<String, Object> args, List<Object>... whitelisted) {
        Map<String, Object> argsCopy = new HashMap<>();
        argsCopy.putAll(args);
        args = argsCopy;

        for (int i = 0; i < whitelisted.length; i++) {
            List<Object> whitelist = whitelisted[i];
            for (Object whitelistedArg : whitelist) {
                args.remove(whitelistedArg);
            }
        }

        if (args.size() == 0) return;

        for (String key : args.keySet()) {
            LOGGER.error("Invalid property \"" + key + "\"");
        }

        throw new IllegalStateException("There are invalid properties in the configuration. Please check thoughday.log.");
    }


    private RunMode getRunMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, Object> runModeParams = configParams.getRunModeParams();
        if (runModeParams.size() != 0 && !runModeParams.containsKey("type")) {
            throw new IllegalStateException("The Run mode doesn't have a type");
        }


        String type = runModeParams.size() != 0 ? String.valueOf(runModeParams.get("type")) : DEFAULT_RUN_MODE;
        Class<? extends RunMode> runModeClass = ReflectionsContainer.getInstance().getRunModeClasses().get(type);

        if (runModeClass == null) {
            throw new IllegalStateException("A run mode with type \"" + type + "\" does not exist");
        }

        return createObject(runModeClass, runModeParams);
    }

    private PublishMode getPublishMode(ConfigParams configParams)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String, Object> publishModeParams = configParams.getPublishModeParams();
        if (publishModeParams.size() != 0 && !publishModeParams.containsKey("type")) {
            throw new IllegalStateException("The Publish mode doesn't have a type");
        }

        String type = publishModeParams.size() != 0 ? String.valueOf(publishModeParams.get("type")) : DEFAULT_PUBLISH_MODE;
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
            // we must keep logging only errors from reflections, in order to avoid irrelevant warning messages when loading an extension into TD.
            if (!loggerConfig.getName().equals("org.reflections.Reflections")) {
                loggerConfig.setLevel(level);
            }
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
        public static final int DEFAULT_PORT = Integer.parseInt(DEFAULT_PORT_STRING);
        public static final String DEFAULT_TIMEOUT_STRING = "180"; // 3 minutes
        public static final long DEFAULT_TIMEOUT = 3 * 60 * 1000l; // 5 minutes
        public static final String DEFAULT_LOG_LEVEL_STRING = "INFO";
        public static final Level DEFAULT_LOG_LEVEL = Level.valueOf(DEFAULT_LOG_LEVEL_STRING);
        public static final String DEFAULT_DRY_RUN = "false";
        public static final String DEFAULT_SAVE_CONFIG = "true";
        private String host;
        private int port;
        private String user;
        private String password;
        private long duration;
        private Map<String, Publisher> publishers;
        private Map<String, Metric> metrics;
        private long timeout;
        private String protocol;
        private boolean installSampleContent = true;
        private String contextPath;
        private Level logLevel = DEFAULT_LOG_LEVEL;
        private boolean dryRun = Boolean.parseBoolean(DEFAULT_DRY_RUN);
        private boolean saveConfig = Boolean.parseBoolean(DEFAULT_SAVE_CONFIG);
        private boolean showSteps = false;

        /**
         * Constructor
         */
        public GlobalArgs() {
            this.publishers = new HashMap<>();
            this.metrics = new LinkedHashMap<>();
            this.port = DEFAULT_PORT;
            this.user = DEFAULT_USER;
            this.password = DEFAULT_PASSWORD;
            this.duration = parseDurationToSeconds(DEFAULT_DURATION);
            this.timeout = DEFAULT_TIMEOUT;
            this.protocol = DEFAULT_PROTOCOL;
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

            //if time unit is not specified, consider it seconds by default.
            if (duration.matches("^[0-9]+$")) {
                duration = duration + "s";
            }

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

        private void updatePublisherName(String oldName, String newName) {
            publishers.put(newName, publishers.remove(oldName));
        }

        private void updateMetricName(String oldName, String newName) {
            metrics.put(newName, metrics.remove(oldName));
        }

        public void addMetric(Metric metric) {
            if (metrics.containsKey(metric.getName())) {
                LOGGER.warn("A metric with this name was already added. Only the last one is taken into consideration.");
            }
            metrics.put(metric.getName(), metric);
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

        public Metric getMetric(String metricName) {
            if (!metrics.containsKey(metricName)) {
                throw new IllegalStateException("Could not find a metric with the name \"" + metricName + "\" to configure it.");
            }
            return metrics.get(metricName);
        }

        public boolean containsPublisher(String publisherName) {
            return publishers.containsKey(publisherName);
        }

        public boolean containsMetric(String metricName) { return metrics.containsKey(metricName); }

        public void removePublisher(String publisherName) {
            Publisher publisher = publishers.remove(publisherName);
            if (publisher == null) {
                throw new IllegalStateException("Could not exclude publisher \"" + publisherName + "\", because there was no publisher configured with that name");
            }
        }

        public void removeMetric(String metricName) {
            Metric metric = metrics.remove(metricName);
            if (metric == null) {
                throw new IllegalStateException("Could not exclude metric \"" + metricName + "\", because there was no metric configured with that name");
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

        /**
         * Returns a list with all the metrics that are going to be published.
         * @return
         */
        public Collection<Metric> getMetrics() {

            Collection<Metric> requiredMetrics = new ArrayList<>();

            //add mandatory metrics
            requiredMetrics.add(new Name());
            requiredMetrics.add(new Timestamp());

            requiredMetrics.addAll(metrics.values());
            return requiredMetrics;
        }

        public Collection<Publisher> getPublishers() {
            return publishers.values();
        }

        // Adders and getters

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

        @ConfigArgSet(required = false, defaultValue = "true", desc = "If true, saves the current configuration into a yaml configuration file.")
        public void setSaveConfig(String saveConfig) {
            this.saveConfig = Boolean.valueOf(saveConfig);
        }

        @ConfigArgGet
        public boolean getSaveConfig() {
            return saveConfig;
        }

        @ConfigArgSet(required = false, defaultValue = "false", desc = "Show test steps in the aggregated publish. (They are always shown in the detailed publish)")
        public void setShowSteps(String showTestSteps) {
            this.showSteps = Boolean.parseBoolean(showTestSteps);
        }

        @ConfigArgGet
        public boolean getShowSteps() {
            return this.showSteps;
        }
    }
}
