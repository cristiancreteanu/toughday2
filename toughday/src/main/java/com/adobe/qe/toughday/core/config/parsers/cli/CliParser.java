package com.adobe.qe.toughday.core.config.parsers.cli;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Name;
import com.adobe.qe.toughday.core.annotations.Tag;
import com.adobe.qe.toughday.core.config.*;
import com.adobe.qe.toughday.core.engine.Engine;
import com.adobe.qe.toughday.core.engine.RunMode;
import com.adobe.qe.toughday.core.engine.PublishMode;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Parser for the command line arguments. It also prints the help message.
 */
public class CliParser implements ConfigurationParser {
    private static final Logger LOGGER = LogManager.getLogger(CliParser.class);

    private static final String HELP_HEADER_FORMAT_WITH_TAGS = "   %-40s %-40s   %s";
    private static final String HELP_HEADER_FORMAT_NO_TAGS = "   %-40s   %s";
    private static final String TEST_CLASS_HELP_HEADER = String.format(HELP_HEADER_FORMAT_WITH_TAGS, "Class", "Tags", "Description");
    private static final String PUBLISH_CLASS_HELP_HEADER = String.format(HELP_HEADER_FORMAT_NO_TAGS, "Class", "Description");
    private static final String SUITE_HELP_HEADER = String.format(HELP_HEADER_FORMAT_WITH_TAGS, "Suite", "Tags", "Description");

    private static Method[] globalArgMethods = Configuration.GlobalArgs.class.getMethods();
    private static Map<Integer, Map<String, ConfigArgSet>> availableGlobalArgs = new HashMap<>();
    private static List<ParserArgHelp> parserArgHelps = new ArrayList<>();
    public final static List<String> parserArgs = new ArrayList<>();
    public static boolean helpRequired = false;

    static {
        for (Method method : globalArgMethods) {
            if (method.getAnnotation(ConfigArgSet.class) != null) {
                ConfigArgSet annotation = method.getAnnotation(ConfigArgSet.class);
                int order = annotation.order();
                if (null == availableGlobalArgs.get(order)) {
                    availableGlobalArgs.put(order, new HashMap<String, ConfigArgSet>());
                }
                Map<String, ConfigArgSet> globalArgMap = availableGlobalArgs.get(order);
                globalArgMap.put(Configuration.propertyFromMethod(method.getName()), annotation);
            }
        }

        for (Class parserClass : ReflectionsContainer.getSubTypesOf(ConfigurationParser.class)) {
            for (Field field : parserClass.getDeclaredFields()) {
                if(field.getType().isAssignableFrom(ParserArgHelp.class)) {
                    try {
                        ParserArgHelp parserArg = (ParserArgHelp) field.get(null);
                        parserArgHelps.add(parserArg);
                        parserArgs.add(parserArg.name());
                    } catch (Exception e) {
                        throw new IllegalStateException("All parser arg help objects must be public and static", e);
                    }
                }
            }
        }
    }

    interface TestFilter {
        Collection<Class<? extends AbstractTest>> filterTests(Collection<Class<? extends AbstractTest>> testClasses);
    }

    interface SuiteFilter {
        Map<String, TestSuite> filterSuites(Map<String, TestSuite> testSuites);
    }

    private static class TagFilter implements TestFilter, SuiteFilter {
        private String tag;

        public TagFilter(String tag) {
            this.tag = tag;
        }

        @Override
        public Collection<Class<? extends AbstractTest>> filterTests(Collection<Class<? extends AbstractTest>> testClasses) {
            Collection<Class<? extends AbstractTest>> filteredTestClasses = new ArrayList<>();
            for (Class<? extends AbstractTest> testClass : testClasses) {
                if(!testClass.isAnnotationPresent(Tag.class))
                    continue;

                if(Arrays.asList(testClass.getAnnotation(Tag.class).tags()).contains(this.tag))
                    filteredTestClasses.add(testClass);
            }
            return filteredTestClasses;
        }

        @Override
        public Map<String, TestSuite> filterSuites(Map<String, TestSuite> testSuites) {
            Map<String, TestSuite> filteredTestSuites = new HashMap<>();
            for (Map.Entry<String, TestSuite> entry : testSuites.entrySet()) {
                if (entry.getValue().getTags().contains(this.tag)) {
                    filteredTestSuites.put(entry.getKey(), entry.getValue());
                }
            }
            return filteredTestSuites;
        }
    }

    private static class AllowAllFilter implements TestFilter, SuiteFilter {
        @Override
        public Map<String, TestSuite> filterSuites(Map<String, TestSuite> testSuites) {
            return testSuites;
        }

        @Override
        public Collection<Class<? extends AbstractTest>> filterTests(Collection<Class<? extends AbstractTest>> testClasses) {
            return testClasses;
        }
    }

    /**
     * Method for parsing and adding a property to the args map.
     * @param propertyAndValue string that contains both the property name and the property value separated by "="
     * @param args map in which the parsed property should be put
     */
    private void parseAndAddProperty(String propertyAndValue, HashMap<String, String> args) {
        String[] res = parseProperty(propertyAndValue);
        args.put(res[0], res[1]);
    }

    private String[] parseProperty(String propertyAndValue) {
        //TODO handle spaces.
        String[] optionValue = propertyAndValue.split("=", 2);
        if (optionValue.length != 1 && optionValue.length != 2) {
            throw new IllegalArgumentException("Properties must have the following form: --property=value or --property. Found: "
                    + propertyAndValue);
        }
        // make the property name lowercase TODO why?
        //String prop = StringUtils.lowerCase(optionValue[0].trim());
        String prop = optionValue[0].trim();
        // default to true if there is no "=" or no value after "="
        String val = (optionValue.length == 2) ? optionValue[1] : "true";

        return new String[] {prop, val};
    }

    private HashMap<String, String> parseObjectProperties(int startIndex, String[] cmdLineArgs) {
        HashMap<String, String> args = new HashMap<>();
        for (int j = startIndex; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
            parseAndAddProperty(cmdLineArgs[j], args);
        }

        return args;
    }

    /**
     * Implementation of parser interface
     * @param cmdLineArgs command line arguments
     * @return a populated ConfigParams object
     */
    public ConfigParams parse(String[] cmdLineArgs) {

        HashMap<String, String> globalArgs = new HashMap<>();
        ConfigParams configParams = new ConfigParams();

        // action parameters
        for (int i = 0; i < cmdLineArgs.length; i++) {
            if (cmdLineArgs[i].startsWith("--")) {
                String arg = cmdLineArgs[i].substring(2);
                if(Actions.isAction(arg)) {
                    Actions action = Actions.fromString(arg);
                    String identifier = cmdLineArgs[i + 1];
                    HashMap<String, String> args = parseObjectProperties(i+2, cmdLineArgs);
                    action.apply(configParams, identifier, args);
                } else if (arg.equals("publishmode")) {
                    configParams.setPublishModeParams(parseObjectProperties(i+1, cmdLineArgs));
                } else if (arg.equals("runmode")) {
                    configParams.setRunModeParams(parseObjectProperties(i+1, cmdLineArgs));
                } else {
                    String[] res = parseProperty(arg);
                    String key = res[0];
                    String val = res[1];
                    // if global param does not exist
                    boolean found = false;
                    for (Map<String, ConfigArgSet> args : availableGlobalArgs.values()) {
                        if (args.containsKey(key)) {
                            found = true;
                            break;
                        }
                    }
                    if (key.contains("help")) {
                        helpRequired = true;
                        continue;
                    } else if (!found && !parserArgs.contains(key)
                            && !key.equals("suite")  && !key.equals("suitesetup")) {
                        throw new IllegalArgumentException("Unrecognized argument --" + key);
                    }

                    globalArgs.put(key, val);
                }
            }
        }
        configParams.setGlobalParams(globalArgs);

        return configParams;
    }

    /**
     * Get the complete description for SuiteSetup parameter.
     */
    private String getSuiteSetupDescription() {
        String suiteSetupDesc = "Setup step for the test suite, where \"val\" can be: ";
        String suiteSetupList = new String();
        for(Class<? extends SuiteSetup> suiteSetupClass : ReflectionsContainer.getInstance().getSuiteSetupClasses().values()) {
            suiteSetupList += " " + suiteSetupClass.getSimpleName();
        }
        return suiteSetupDesc  + (suiteSetupList.isEmpty() ? "(none)" : suiteSetupList);
    }

    /**
     * Method for printing the help message
     */
    public void printHelp() {
        // print the shorter part of the help
        printShortHelp(true);

        // Follow up with the rest
        printExtraHelp();
    }

    public static void printTestClasses(TestFilter filter) {
        System.out.println();
        System.out.println("Available test classes:");
        System.out.println(TEST_CLASS_HELP_HEADER);
        for (Class<? extends AbstractTest> testClass : filter.filterTests(ReflectionsContainer.getInstance().getTestClasses().values())){
            printClass(testClass, false, true, false);
        }
    }

    public static void printPublisherClasses() {
        System.out.println();
        System.out.println("Available publisher classes:");
        System.out.println(PUBLISH_CLASS_HELP_HEADER);
        for (Class<? extends Publisher> publisherClass : ReflectionsContainer.getInstance().getPublisherClasses().values()) {
            printClass(publisherClass, false, false, false);
        }
    }

    public static void printExtraHelp() {
        printTestClasses(new AllowAllFilter());
        printPublisherClasses();
    }

    public boolean printHelp(String[] cmdLineArgs) {
        if (cmdLineArgs[0].equals("--help_full")) {
            printHelp();
            return true;
        } else if (cmdLineArgs[0].equals("--help_tests")) {
            printTestClasses(new AllowAllFilter());
            return true;
        } else if (cmdLineArgs[0].equals("--help_publish")) {
            printPublisherClasses();
            return true;
        } else if ( (cmdLineArgs[0].equals("--help") && cmdLineArgs.length > 1 )) {
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(cmdLineArgs[1])) {
                Class<? extends AbstractTest> testClass = ReflectionsContainer.getInstance().getTestClasses().get(cmdLineArgs[1]);
                System.out.println(TEST_CLASS_HELP_HEADER);
                printClass(testClass, true, true, false);
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(cmdLineArgs[1])) {
                Class<? extends Publisher> publisherClass = ReflectionsContainer.getInstance().getPublisherClasses().get(cmdLineArgs[1]);
                System.out.println(PUBLISH_CLASS_HELP_HEADER);
                printClass(publisherClass, true, false, false);
            } else if (cmdLineArgs[1].startsWith("--suite=")) {
                System.out.println(SUITE_HELP_HEADER);
                printTestSuite(new PredefinedSuites(), cmdLineArgs[1].split("=")[1], true, true);
            } else if (cmdLineArgs[1].startsWith("--tag=")) {
                printTagHelp(cmdLineArgs[1].split("=")[1]);
            } else if (cmdLineArgs[1].equals("--runmode")) {
                if (cmdLineArgs.length < 3) {
                    System.out.println("Wrong help command format.");
                    printShortHelp();
                    return true;
                }

                String[] tmp = cmdLineArgs[2].split("=");
                if(!tmp[0].equals("type"))
                    throw new IllegalArgumentException("Cannot print information about a run mode if no type is specified");
                Class klass = ReflectionsContainer.getInstance().getRunModeClasses().get(tmp[1]);
                if(klass == null) {
                    throw new IllegalArgumentException("No run mode found with type: \"" + tmp[1] + "\"");
                }
                printClass(klass, true, false, true);
                return true;
            } else if (cmdLineArgs[1].equals("--publishmode")) {
                if (cmdLineArgs.length < 3) {
                    System.out.println("Wrong help command format.");
                    printShortHelp();
                    return true;
                }

                String[] tmp = cmdLineArgs[2].split("=");
                if(!tmp[0].equals("type"))
                    throw new IllegalArgumentException("Cannot print information about a publish mode if no type is specified");

                Class klass = ReflectionsContainer.getInstance().getPublishModeClasses().get(tmp[1]);
                if(klass == null) {
                    throw new IllegalArgumentException("No publish mode found with type: \"" + tmp[1] + "\"");
                }
                printClass(klass, true, false, true);
                return true;

            } else {
                System.out.println("Could not find any test or publisher \"" + cmdLineArgs[1] + "\"");
            }
            return true;
        }

        for (String cmdLineArg : cmdLineArgs) {
            if (cmdLineArg.equals("--help")) {
                printShortHelp();
                return true;
            }
        }
        return false;
    }

    public void printShortHelp(boolean printSuitesTests) {
        System.out.println("Usage: java -jar toughday.jar [--help | --help_full | --help_tests | --help_publish] [<global arguments> | <actions>]");
        System.out.println("Running the jar with no parameters or '--help' prints the help.");
        System.out.println("Use '--help_full' to print full help.");
        System.out.println("Use '--help_tests' to print all the test classes.");
        System.out.println("Use '--help_publish' to print all the publisher classes.");
        System.out.println("Use '--help $TestClass/$PublisherClass' to view all configurable properties for that test/publisher");
        System.out.println("Use '--help --suite=$SuiteName' to find information about a test suite");
        System.out.println("Use '--help --tag=$Tag' to find all items that have a the specified tag");
        System.out.println("Use '--help --runmode/publishmode type=$Mode' to find information about a run/publish mode");

        System.out.println("\r\nExamples: \r\n");
        System.out.println("\t java -jar toughday.jar --host=localhost --port=4502");
        System.out.println("\t java -jar toughday.jar --runmode type=normal concurrency=20 --host=localhost --port=4502");

        System.out.println("\r\nGlobal arguments:");

        for (Integer order : availableGlobalArgs.keySet()) {
            Map<String, ConfigArgSet> paramGroup = availableGlobalArgs.get(order);
            for (String param : paramGroup.keySet()) {
                System.out.printf("\t--%-32s\t Default: %s - %s\r\n",
                        param + "=val", paramGroup.get(param).defaultValue(), paramGroup.get(param).desc());
            }
        }
        for (ParserArgHelp parserArgHelp : parserArgHelps) {
            System.out.printf("\t--%-32s\t Default: %s - %s\r\n",
                    parserArgHelp.name() + "=val", parserArgHelp.defaultValue(), parserArgHelp.description());
        }

        //System.out.printf("\t%-32s\t %s\r\n", "--suitesetup=val", getSuiteSetupDescription());
        System.out.printf("\t%-32s\t %s\r\n", "--suite=val",
                "Default: toughday - Where \"val\" can be one or a list (separated by commas) of the predefined suites");

        System.out.println("\r\nAvailable run modes (--runmode):");
        for(Map.Entry<String, Class<? extends RunMode>> runMode : ReflectionsContainer.getInstance().getRunModeClasses().entrySet()) {
            Description description = runMode.getValue().getAnnotation(Description.class);
            System.out.printf("\ttype=%-71s %s\r\n", runMode.getKey(), description != null ? description.desc() : "");
        }

        System.out.println("\r\nAvailable publish modes (--publishmode):");
        for(Map.Entry<String, Class<? extends PublishMode>> publishMode : ReflectionsContainer.getInstance().getPublishModeClasses().entrySet()) {
            Description description = publishMode.getValue().getAnnotation(Description.class);
            System.out.printf("\ttype=%-71s %s\r\n", publishMode.getKey(), description != null ? description.desc() : "");
        }

        System.out.println("\r\nAvailable actions:");
        for (Actions action : Actions.values()) {
            System.out.printf("\t--%-71s %s\r\n", action.value() + " " + action.actionParams(), action.actionDescription());
        }

        printTestSuites(new AllowAllFilter(), printSuitesTests);
    }

    private static void printTagHelp(String tag) {
        if(StringUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("Tag was empty");
        }

        TagFilter tagFilter = new TagFilter(tag);
        printTestSuites(tagFilter, false);

        printTestClasses(tagFilter);
    }

    public void printShortHelp() {
        printShortHelp(false);
    }

    private static void printClassProperty(String name, boolean required, String defaultValue, String description) {
        System.out.println(String.format("\t%-32s %-64s %-32s",
                name + "=val" + (required ? "" : " (optional)"),
                defaultValue,
                description));
    }

    private static void printClass(Class klass, boolean printProperties, boolean printTags, boolean lowerCaseClass) {
        String name = lowerCaseClass ? klass.getSimpleName().toLowerCase() : klass.getSimpleName();
        String desc = "";
        String tags = "";
        if (klass.isAnnotationPresent(Name.class)) {
            Name d = (Name) klass.getAnnotation(Name.class);
            name = name + " [" + d.name() + "]";
        }
        if (klass.isAnnotationPresent(Description.class)) {
            Description d = (Description) klass.getAnnotation(Description.class);
            desc = d.desc();
        }

        if (klass.isAnnotationPresent(Tag.class)) {
            Tag tag = (Tag) klass.getAnnotation(Tag.class);
            tags = Joiner.on(", ").join(tag.tags());
        }

        if(!printTags) {
            System.out.println(String.format(" - %-40s - %s", name, desc));
        } else {
            System.out.println(String.format(" - %-40s %-40s - %s", name, tags, desc));
        }

        if (printProperties) {
            System.out.println();
            System.out.println(String.format("\t%-32s %-64s %-32s", "Property", "Default", "Description"));
            for (Method method : klass.getMethods()) {
                if (method.getAnnotation(ConfigArgSet.class) != null) {
                    ConfigArgSet annotation = method.getAnnotation(ConfigArgSet.class);
                    printClassProperty(Configuration.propertyFromMethod(method.getName()),
                            annotation.required(),
                            annotation.defaultValue(),
                            annotation.desc());
                }
            }
            if(AbstractTest.class.isAssignableFrom(klass)) {
                printClassProperty("weight", false, "1", "The weight of this test");
                printClassProperty("timeout", false, String.valueOf(Configuration.GlobalArgs.DEFAULT_TIMEOUT),
                        "How long a test will run before it will be interrupted and marked as failed. Expressed in seconds");
                printClassProperty("count", false, "none", "The approximate number of times this test should run");
            }
        }
    }

    private static void printTestSuites(SuiteFilter filter, boolean withTests) {
        PredefinedSuites predefinedSuites = new PredefinedSuites();
        System.out.println("\r\nPredefined suites");
        System.out.println(SUITE_HELP_HEADER);
        for (String testSuiteName : filter.filterSuites(predefinedSuites).keySet()) {
            printTestSuite(predefinedSuites, testSuiteName, withTests, false);
        }
    }

    private static void printTestSuite(PredefinedSuites predefinedSuites, String testSuiteName, boolean withTests, boolean withTestProperties) {
        TestSuite testSuite = predefinedSuites.get(testSuiteName);
        if(testSuite == null) {
            System.out.println("Cannot find a test predefined test suite named " + testSuiteName);
            return;
        }

        System.out.println(String.format(" - %-40s %-40s - %s", testSuiteName, Joiner.on(", ").join(testSuite.getTags()), testSuite.getDescription()));
        if (withTests) {
            for (AbstractTest test : testSuite.getTests()) {
                System.out.printf("\t%-32s\r\n", test.getFullName() + " [" + test.getClass().getSimpleName() + "]");
                if (withTestProperties) {
                    try {
                        Engine.printObject(testSuite, System.out, test);
                    } catch (Exception e) {
                        LOGGER.error("Exception while printing the test suite.", e);
                    }
                }
            }
        }
    }
}
