package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.*;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for the command line arguments. It also prints the help message.
 */
public class CliParser implements ConfigurationParser {
    private static final List<String> actions;
    private static final HashMap<String, String> actionsParams;
    private static final HashMap<String, String> actionsDescription;

    static {
        actions = new ArrayList<>();
        actions.add("add");
        actions.add("config");
        actions.add("exclude");

        actionsParams = new HashMap<>();
        actionsParams.put("add", "TestClass/PublisherClass property1=val property2=val");
        actionsParams.put("config", "TestName property1=val property2=val");
        actionsParams.put("exclude", "TestName");

        actionsDescription = new HashMap<>();
        actionsDescription.put("add", "Add a test to the suite or a publisher");
        actionsDescription.put("config", "Override parameters for a test from a predefined suite");
        actionsDescription.put("exclude", "Exclude a test from a predefined suite");
    }

    private static Map<String, String> availableGlobalArgs = new HashMap<>();
    static {
        for (Method method : Configuration.GlobalArgs.class.getMethods()) {
            if (method.getAnnotation(ConfigArg.class) != null) {
                ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                availableGlobalArgs.put(Configuration.propertyFromMethod(method.getName()), annotation.desc());
            }
        }
    }

    private static String getActionParams(String action) {
        return actionsParams.get(action) != null ? actionsParams.get(action) : "";
    }

    private static String getActionDescription(String action) {
        return actionsDescription.get(action) != null ? actionsDescription.get(action) : "";
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
        // make the property name lowercase
        String prop = StringUtils.lowerCase(optionValue[0].trim());
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
        for (String arg : cmdLineArgs) {
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                if (!actions.contains(arg)) {
                    String[] res = parseProperty(arg);
                    String key = res[0];
                    String val = res[1];
                    // if global param does not exist
                    if (!availableGlobalArgs.containsKey(key) && !key.equals("suite")  && !key.equals("SuiteSetup")) {
                        throw new IllegalArgumentException("Unrecognized argument --" + key);
                    }
                    globalArgs.put(key, val);
                }
            }
        }
        configParams.setGlobalParams(globalArgs);


        for (int i = 0; i < cmdLineArgs.length; i++) {
            if (cmdLineArgs[i].startsWith("--")) {
                String action = cmdLineArgs[i].substring(2);

                if (actions.contains(action)) {
                    String identifier = cmdLineArgs[i + 1];
                    HashMap<String, String> args = new HashMap<>();
                    for (int j = i + 2; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
                        parseAndAddProperty(cmdLineArgs[j], args);
                        i = j;
                    }
                    if (action.equals("add")) {
                        if (ReflectionsContainer.getInstance().getTestClasses().containsKey(identifier)) {
                            configParams.addTest(identifier, args);
                        } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                            configParams.addPublisher(identifier, args);
                        } else {
                            throw new IllegalArgumentException("Unknown publisher or test class: " + identifier);
                        }
                    } else if (action.equals("config")) {
                        configParams.configTest(identifier, args);
                    } else if (action.equals("exclude")) {
                        if (args.size() != 0) {
                            throw new IllegalArgumentException("--exclude cannot have properties for identifier: " + identifier);
                        }

                        if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                            //TODO exclude publishers
                        } else {
                            configParams.excludeTest(identifier);
                        }
                    }
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
        return suiteSetupDesc;
    }



    /**
     * Method for printing the help message
     */
    public void printHelp() {
        // print the shorter part of the help
        printShortHelp();

        // Follow up with the rest
        printTestsHelp();
    }

    public void printTestsHelp() {
        PredefinedSuites predefinedSuites = new PredefinedSuites();
        System.out.println("\r\nPredefined suites");
        for (String testSuiteName : predefinedSuites.keySet()) {
            TestSuite testSuite = predefinedSuites.get(testSuiteName);
            System.out.printf("\t%-32s\t %-32s\r\n", testSuiteName, testSuite.getDescription());
            for (AbstractTest test : testSuite.getTests()) {
                System.out.printf("\t\t%-32s\r\n", test.getName() + " [" + test.getClass().getSimpleName() + "]");
                //TODO print default properties
            }
        }

        System.out.println();
        System.out.println("Available test classes:");
        for (Class<? extends AbstractTest> testClass : ReflectionsContainer.getInstance().getTestClasses().values()){
            System.out.println("\t" + testClass.getSimpleName());
            for (Method method : testClass.getMethods()) {
                if (method.getAnnotation(ConfigArg.class) != null) {
                    ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                    printTestClass(Configuration.propertyFromMethod(method.getName()), annotation.required(), annotation.desc());
                }
            }
            printTestClass("weight", true, "");
            printTestClass("timeout", false, "");
        }

        System.out.println();
        System.out.println("Available publisher classes:");
        for (Class<? extends Publisher> publisherClass : ReflectionsContainer.getInstance().getPublisherClasses().values()) {
            System.out.printf("\t%-32s\r\n", publisherClass.getSimpleName());
            for (Method method : publisherClass.getMethods()) {
                if (method.getAnnotation(ConfigArg.class) != null) {
                    ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                    printTestClass(Configuration.propertyFromMethod(method.getName()), annotation.required(), annotation.desc());
                }
            }
        }
    }

    public void printShortHelp() {
        System.out.println("Usage: java -jar toughday2.jar [--help | --print_tests] [<global arguments> | <actions>]");
        System.out.println("Running the jar with no parameters or '--help' prints the help. Use '--print_tests' to print full help.\r\n");
        System.out.println("Global arguments:");

        for (String param : availableGlobalArgs.keySet()) {
                System.out.printf("\t--%-32s\t %s\r\n", param + "=val", availableGlobalArgs.get(param));
        }
        System.out.printf("\t%-32s\t %s\r\n", "--SuiteSetup=val", getSuiteSetupDescription());
        System.out.printf("\t%-32s\t %s\r\n", "--suite=val",
                "where \"val\" can be one, or more predefined suite. (use comas to separate them)");

        System.out.println("\r\nAvailable actions:");
        for (String action : actions) {
            System.out.printf("\t--%-71s %s\r\n", action + " " + getActionParams(action), getActionDescription(action));
        }
    }

    private static void printTestClass(String name, boolean required, String description) {
        System.out.printf("\t\t%-32s %-32s %-32s\r\n", name + "=val", "required: " + (required ? "yes" : "no"), description);
    }

}
