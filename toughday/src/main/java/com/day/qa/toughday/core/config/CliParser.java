package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 27/08/15.
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
        actionsDescription.put("add", "add a test to the suite or a publisher");
        actionsDescription.put("config", "override parameters for a test from a predefined suite");
        actionsDescription.put("exclude", "exclude a test from a predefined suite");
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
        //TODO handle spaces.
        String[] optionValue = propertyAndValue.split("=", 2);
        if(optionValue.length != 2)
            throw new IllegalArgumentException("Properties must have the following form: property=value. Found: " + propertyAndValue);
        args.put(optionValue[0], optionValue[1]);
    }

    /**
     * Implementation of parser interface
     * @param cmdLineArgs command line arguments
     * @return a populated ConfigParams object
     */
    public ConfigParams parse(String[] cmdLineArgs) {
        HashMap<String, String> globalArgs = new HashMap<>();
        ConfigParams configParams = new ConfigParams();
        for(String arg : cmdLineArgs) {
            if(arg.startsWith("--")) {
                arg = arg.substring(2);
                if (!actions.contains(arg)) {
                    parseAndAddProperty(arg, globalArgs);
                }
            }
        }
        configParams.setGlobalParams(globalArgs);


        for(int i = 0; i < cmdLineArgs.length; i++) {
            if(cmdLineArgs[i].startsWith("--")) {
                String action = cmdLineArgs[i].substring(2);
                if(actions.contains(action)) {
                    String identifier = cmdLineArgs[i + 1];
                    HashMap<String, String> args = new HashMap<>();
                    for (int j = i + 2; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
                        parseAndAddProperty(cmdLineArgs[j], args);
                        i = j;
                    }
                    if (action.equals("add")) {
                        if(ReflectionsContainer.getInstance().getTestClasses().containsKey(identifier)) {
                            configParams.addTest(identifier, args);
                        } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                            configParams.addPublisher(identifier, args);
                        } else {
                            throw new IllegalArgumentException("Unknown publisher or test class: " + identifier);
                        }
                    } else if (action.equals("config")) {
                        configParams.configTest(identifier, args);
                    } else if (action.equals("exclude")) {
                        if(args.size() != 0) {
                            throw new IllegalArgumentException("--exclude cannot have properties for identifier: " + identifier);
                        }

                        if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                            //TODO
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
        String suiteSetupDesc = "setup step for the test suite. where \"val\" can be: ";
        for(Class<? extends SuiteSetup> suiteSetupClass : ReflectionsContainer.getInstance().getSuiteSetupClasses().values()) {
            suiteSetupDesc += " " + suiteSetupClass.getSimpleName();
        }
        return suiteSetupDesc;
    }

    /**
     * Method for printing the help message
     */
    public void printHelp() {
        System.out.println("usage: java -jar <toughday-jar> <global arguments>|<actions>");
        System.out.println("global arguments:");

        for(Method method : Configuration.GlobalArgs.class.getMethods()) {
            if(method.getAnnotation(ConfigArg.class) != null) {
                ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                System.out.println("\t--" + Configuration.propertyFromMethod(method.getName()) + "=val"
                        + "\t\t\t\t" + annotation.desc());
            }
        }
        System.out.println("\t--SuiteSetup=val" + "\t\t\t\t" + getSuiteSetupDescription());
        System.out.println("\t--Suite=val" + "\t\t\t\t" + "where \"val\" can be one, or more predefined suite. (use comas to separate them)");

        System.out.println();
        System.out.println("available actions:");
        for(String action : actions) {
            System.out.println("\t--" + action + " " + getActionParams(action)
                    + "\t\t\t\t " + getActionDescription(action));
        }

        PredefinedSuites predefinedSuites = new PredefinedSuites();
        System.out.println();
        System.out.println("predefined suites");
        for(String testSuiteName : predefinedSuites.keySet()) {
            TestSuite testSuite = predefinedSuites.get(testSuiteName);
            System.out.println("\t" + testSuiteName + "\t\t\t\t" + testSuite.getDescription());
            for(AbstractTest test : testSuite.getTests()) {
                System.out.println("\t\t" + test.getName() + " [" + test.getClass().getSimpleName() + "]");
                //TODO print default properties
            }
        }

        System.out.println();
        System.out.println("available test classes:");
        for(Class<? extends AbstractTest> testClass : ReflectionsContainer.getInstance().getTestClasses().values()){
            System.out.println("\t" + testClass.getSimpleName());
            for(Method method : testClass.getMethods()) {
                if(method.getAnnotation(ConfigArg.class) != null) {
                    ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                    System.out.println("\t\t" + Configuration.propertyFromMethod(method.getName()) + "=val"
                            + "\t\t\t\t" + "required=" + (annotation.required() ? "true" : "false") + "\t\t\t\t" + annotation.desc());
                }
            }
            System.out.println("\t\t" + "Weight=val" + "\t\t\t\t" + "required=true");
            System.out.println("\t\t" + "Timeout=val"+ "\t\t\t\t" + "required=false");
        }

        System.out.println();
        System.out.println("available publishers classes:");
        for(Class<? extends Publisher> publisherClass : ReflectionsContainer.getInstance().getPublisherClasses().values()) {
            System.out.println("\t" + publisherClass.getSimpleName());
            for (Method method : publisherClass.getMethods()) {
                if (method.getAnnotation(ConfigArg.class) != null) {
                    ConfigArg annotation = method.getAnnotation(ConfigArg.class);
                    System.out.println("\t\t" + Configuration.propertyFromMethod(method.getName()) + "=val"
                            + "\t\t\t\t" + "required=" + (annotation.required() ? "true" : "false") + "\t\t\t\t" + annotation.desc());
                }
            }
        }
    }

}
