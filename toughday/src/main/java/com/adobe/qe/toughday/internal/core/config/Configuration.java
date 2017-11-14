package com.adobe.qe.toughday.internal.core.config;

import com.adobe.qe.toughday.Main;
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
        if (globalArgs.getMetrics().size() == 0) {
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

}