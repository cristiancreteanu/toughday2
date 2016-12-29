package com.adobe.qe.toughday.core.config.parsers.cli;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Parser for the command line arguments. It also prints the help message.
 */
public class CliParser implements ConfigurationParser {

    private static Method[] globalArgMethods = Configuration.GlobalArgs.class.getMethods();
    private static Map<Integer, Map<String, ConfigArg>> availableGlobalArgs = new HashMap<>();
    private static List<ParserArgHelp> parserArgHelps = new ArrayList<>();
    private static List<String> parserArgs = new ArrayList<>();

    static {
        for (Method method : globalArgMethods) {
            if (method.getAnnotation(ConfigArg.class) != null) {
                ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                int order = annotation.order();
                if (null == availableGlobalArgs.get(order)) {
                    availableGlobalArgs.put(order, new HashMap<String, ConfigArg>());
                }
                Map<String, ConfigArg> globalArgMap = availableGlobalArgs.get(order);
                globalArgMap.put(Configuration.propertyFromMethod(method.getName()), annotation);
            }
        }

        for (Class parserClass : ReflectionsContainer.getReflections().getSubTypesOf(ConfigurationParser.class)) {
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

    // Fields
    private PredefinedSuites predefinedSuites;

    /**
     * Constructor
     */
    public CliParser() {
        this.predefinedSuites = new PredefinedSuites();
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

    /**
     * Implementation of parser interface
     * @param cmdLineArgs command line arguments
     * @return a populated ConfigParams object
     */
    public ConfigParams parse(String[] cmdLineArgs) {
        HashMap<String, String> globalArgs = new HashMap<>();
        ConfigParams configParams = new ConfigParams();

        // Global parameters
        for (String arg : cmdLineArgs) {
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                if (!Actions.isAction(arg)) {
                    String[] res = parseProperty(arg);
                    String key = res[0];
                    String val = res[1];
                    // if global param does not exist
                    boolean found = false;
                    for (Map<String, ConfigArg> args : availableGlobalArgs.values()) {
                        if (args.containsKey(key)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found && !parserArgs.contains(key)
                            && !key.equals("suite")  && !key.equals("SuiteSetup")
                            && !key.equals("help")) {
                        throw new IllegalArgumentException("Unrecognized argument --" + key);
                    }
                    globalArgs.put(key, val);
                }
            }
        }
        configParams.setGlobalParams(globalArgs);


        // action parameters
        for (int i = 0; i < cmdLineArgs.length; i++) {
            if (cmdLineArgs[i].startsWith("--")) {
                String actionString = cmdLineArgs[i].substring(2);
                if(Actions.isAction(actionString)) {
                    Actions action = Actions.fromString(actionString);
                    String identifier = cmdLineArgs[i + 1];
                    HashMap<String, String> args = new HashMap<>();
                    for (int j = i + 2; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
                        parseAndAddProperty(cmdLineArgs[j], args);
                        i = j;
                    }

                    action.apply(configParams, identifier, args);
                }
            }
        }

        return configParams;
    }

    /**
     * Get the complete description for SuiteSetup parameter.
     */
    private String getSuiteSetupDescription() {
        String suiteSetupDesc = "Setup step for the test suite, where \"val\" can be: ";
        for(Class<? extends SuiteSetup> suiteSetupClass : ReflectionsContainer.getInstance().getSuiteSetupClasses().values()) {
            suiteSetupDesc += " " + suiteSetupClass.getSimpleName();
        }
        return (suiteSetupDesc.isEmpty() ? "(none)" : suiteSetupDesc);
    }



    /**
     * Method for printing the help message
     */
    public void printHelp() {
        // print the shorter part of the help
        printShortHelp(false);

        // Follow up with the rest
        printExtraHelp();
    }

    public void printExtraHelp() {
        System.out.println("\r\nPredefined suites");
        for (String testSuiteName : predefinedSuites.keySet()) {
            TestSuite testSuite = predefinedSuites.get(testSuiteName);
            System.out.printf("\t%-32s\t %-32s\r\n", testSuiteName, testSuite.getDescription());
            for (AbstractTest test : testSuite.getTests()) {
                System.out.printf("\t\t%-32s\r\n", test.getFullName() + " [" + test.getClass().getSimpleName() + "]");
                //TODO print default properties
            }
        }

        System.out.println();
        System.out.println("Available test classes:");
        for (Class<? extends AbstractTest> testClass : ReflectionsContainer.getInstance().getTestClasses().values()){
            printClass(testClass, false);
        }

        System.out.println();
        System.out.println("Available publisher classes:");
        for (Class<? extends Publisher> publisherClass : ReflectionsContainer.getInstance().getPublisherClasses().values()) {
            printClass(publisherClass, false);
        }
    }

    public boolean printHelp(String[] cmdLineArgs) {
        if (cmdLineArgs.length == 0 || (cmdLineArgs.length == 1 && cmdLineArgs[0].equals("--help"))) {
            printShortHelp();
            return true;
        } else if (cmdLineArgs.length == 1 && cmdLineArgs[0].equals("--help_full")) {
            printHelp();
            return true;
        } else if (cmdLineArgs.length == 2 && cmdLineArgs[0].equals("--help") &&
                ReflectionsContainer.getInstance().getTestClasses().containsKey(cmdLineArgs[1])) {
            Class<? extends AbstractTest> testClass = ReflectionsContainer.getInstance().getTestClasses().get(cmdLineArgs[1]);
            printClass(testClass, true);
            return true;
        } else if (cmdLineArgs.length == 2 && cmdLineArgs[0].equals("--help") &&
                ReflectionsContainer.getInstance().getPublisherClasses().containsKey(cmdLineArgs[1])) {
            Class<? extends Publisher> publisherClass = ReflectionsContainer.getInstance().getPublisherClasses().get(cmdLineArgs[1]);
            printClass(publisherClass, true);
            return true;
        }
        return false;
    }

    public void printShortHelp(boolean printSuites) {
        System.out.println("Usage: java -jar toughday.jar [--help | --help_full] [<global arguments> | <actions>]");
        System.out.println("Running the jar with no parameters or '--help' prints the help.");
        System.out.println("Use '--help_full' to print full help.");
        System.out.println("Use '--help $TestClass/$PublisherClass' to view all configurable properties for that test/publisher");

        System.out.println("\r\nExamples: \r\n");
        System.out.println("\t java -jar toughday.jar --suite=tree_authoring --host=localhost --port=4502");
        System.out.println("\t java -jar toughday.jar --suite=tree_authoring --config AuthoringTreeTest pageTemplate=/apps/my/mytemplate");

        System.out.println("\r\nGlobal arguments:");

        for (Integer order : availableGlobalArgs.keySet()) {
            Map<String, ConfigArg> paramGroup = availableGlobalArgs.get(order);
            for (String param : paramGroup.keySet()) {
                System.out.printf("\t--%-32s\t Default: %s - %s\r\n",
                        param + "=val", paramGroup.get(param).defaultValue(), paramGroup.get(param).desc());
            }
        }
        for (ParserArgHelp parserArgHelp : parserArgHelps) {
            System.out.printf("\t--%-32s\t Default: %s - %s\r\n",
                    parserArgHelp.name() + "=val", parserArgHelp.defaultValue(), parserArgHelp.description());
        }

        System.out.printf("\t%-32s\t %s\r\n", "--SuiteSetup=val", getSuiteSetupDescription());
        System.out.printf("\t%-32s\t %s\r\n", "--suite=val",
                "where \"val\" can be one, or more predefined suite. (use commas to separate them)");

        if (printSuites) {
            System.out.println("\r\nPredefined suites");
            for (String testSuiteName : predefinedSuites.keySet()) {
                TestSuite testSuite = predefinedSuites.get(testSuiteName);
                System.out.printf("\t%-32s\t %-32s\r\n", testSuiteName, testSuite.getDescription());
            }
        }

        System.out.println("\r\nAvailable actions:");
        for (Actions action : Actions.values()) {
            System.out.printf("\t--%-71s %s\r\n", action.value() + " " + action.actionParams(), action.actionDescription());
        }
    }

    public void printShortHelp() {
        printShortHelp(true);
    }

    private static void printClassProperty(String name, boolean required, String defaultValue, String description) {
        System.out.println(String.format("\t%-32s %-64s %-32s",
                name + "=val" + (required ? "" : " (optional)"),
                defaultValue,
                description));
    }

    private static void printClass(Class klass, boolean printProperties) {
        String name = klass.getSimpleName();
        String desc = "";
        if (klass.isAnnotationPresent(Description.class)) {
            Description d = (Description) klass.getAnnotation(Description.class);
            name = name + " [" + d.name() + "]";
            desc = d.desc();
        }
        System.out.println(String.format(" - %-40s - %s", name, desc));
        if (printProperties) {
            for (Method method : klass.getMethods()) {
                if (method.getAnnotation(ConfigArg.class) != null) {
                    ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                    printClassProperty(Configuration.propertyFromMethod(method.getName()),
                            annotation.required(),
                            annotation.defaultValue(),
                            annotation.desc());
                }
            }
            if(klass.isAssignableFrom(AbstractTest.class)) {
                printClassProperty("weight", true, "1", "The weight of this test");
                printClassProperty("timeout", false, String.valueOf(Configuration.GlobalArgs.DEFAULT_TIMEOUT),
                        "Time in milliseconds after which the test is interrupted");
                printClassProperty("count", false, "none", "The number of times this test should run");
            }
        }
    }
}
