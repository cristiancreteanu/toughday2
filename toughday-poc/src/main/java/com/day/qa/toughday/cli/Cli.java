package com.day.qa.toughday.cli;

import com.day.qa.toughday.TestSuite;
import com.day.qa.toughday.publishers.Publisher;
import com.day.qa.toughday.tests.AbstractTest;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tuicu on 27/08/15.
 */
public class Cli {
    private HashMap<String, Class<? extends AbstractTest>> testClasses;
    private HashMap<String, Class<? extends Publisher>> publisherClasses;
    private Options options; //used for printing help message only

    public Cli() {
        testClasses = new HashMap<>();
        publisherClasses = new HashMap<>();
        options = new Options();
        options = new Options();
        options.addOption(Option.builder().longOpt("Duration=val")
                .desc("how long will toughday run")
                .build());
        options.addOption(Option.builder().longOpt("WaitTime=val")
                .desc("wait time between two consecutive test runs for a user in milliseconds")
                .build());
        options.addOption(Option.builder().longOpt("Concurrency=val")
                .desc("number of concurrent users")
                .build());
        Reflections reflections = new Reflections("com.day.qa");
        for(Class<? extends AbstractTest> testClass : reflections.getSubTypesOf(AbstractTest.class)) {
            if(testClasses.containsKey(testClass.getSimpleName()))
                throw new IllegalStateException("A test class with this name already exists here: "
                        + testClasses.get(testClass.getSimpleName()).getName());
            testClasses.put(testClass.getSimpleName(), testClass);
            options.addOption(getOptionFromTestClass(testClass));
        }

        for(Class<? extends Publisher> publisherClass : reflections.getSubTypesOf(Publisher.class)) {
            if(publisherClasses.containsKey(publisherClass.getSimpleName()))
                throw new IllegalStateException("A publisher class with this name already exists here: "
                        + publisherClasses.get(publisherClass.getSimpleName()).getName());
            publisherClasses.put(publisherClass.getSimpleName(), publisherClass);
            options.addOption(getOptionFromPublisher(publisherClass));
        }
    }

    private String propertyFromMethod(String methodName) {
        return methodName.startsWith("set") ? methodName.substring(3) : methodName;
    }

    private String getOptionArgNameForClass(Class<?> klass) {
        ArrayList<Method> properties = new ArrayList<>();
        for(Method method : klass.getMethods()) {
            if(method.getAnnotation(CliArg.class) != null) {
                if(method.getParameterTypes().length != 1 || !method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    throw new IllegalStateException("Setters annotated with CliArg must have one parameter of type String");
                }
                properties.add(method);
            }
        }

        String argName = "";

        if(properties.size() != 0) {
            for (int i = 0; i < properties.size(); i++) {
                if (i != 0)
                    argName += "> <";
                Method current = properties.get(i);
                String currentArgName = propertyFromMethod(current.getName()) + "=val";
                currentArgName = current.getAnnotation(CliArg.class).required() ? currentArgName : "[" + currentArgName + "]";
                argName += currentArgName;
            }
        }
        return argName;
    }

    private Option getOptionFromTestClass(Class<? extends AbstractTest> testClass) {
        Option.Builder builder = Option.builder()
                .longOpt(testClass.getSimpleName())
                .desc("add a " + testClass.getSimpleName() + " test to the suite");

        String argName = getOptionArgNameForClass(testClass);
        argName = "Weight=val" + (argName.length() != 0 ? "> <" + argName : argName);
        builder.hasArgs().argName(argName);

        return builder.build();
    }

    public Option getOptionFromPublisher(Class<? extends Publisher> publisher) {
        Option.Builder builder = Option.builder()
                .longOpt(publisher.getSimpleName())
                .desc("add a " + publisher.getSimpleName() + " publisher");

        String argName = getOptionArgNameForClass(publisher);
        return argName.length() == 0 ? builder.build() : builder.hasArgs().argName(argName).build();
    }


    public <T> T createObject(Class<? extends T> classObject, HashMap<String, String> args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = null;
        for(Constructor iterator : classObject.getConstructors()) {
            if(iterator.getParameterTypes().length == 0) {
                constructor = iterator;
            }
        }


        if(constructor == null) {
            throw new IllegalStateException(classObject.getSimpleName() + " class must have a constructor without arguments");
        }
        T testObject = (T) constructor.newInstance();
        for(Method method : classObject.getMethods()) {
            CliArg annotation = method.getAnnotation(CliArg.class);
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
            method.invoke(testObject, value);
        }
        return testObject;
    }

    private void parseAndAddProperty(String propertyAndValue, HashMap<String, String> args) {
        String[] optionValue = propertyAndValue.split("=");
        if(optionValue.length != 2)
            throw new IllegalArgumentException("Properties must have the following form: property=value. Found: " + propertyAndValue);
        args.put(optionValue[0], optionValue[1]);
    }

    public TestSuite createTestSuite(String[] cmdLineArgs) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        HashMap<String, String> suiteArgs = new HashMap<>();

        for(String arg : cmdLineArgs) {
            if(arg.startsWith("--")) {
                arg = arg.substring(2);
                if (!(testClasses.containsKey(arg) || publisherClasses.containsKey(arg))) {
                    parseAndAddProperty(arg, suiteArgs);
                }
            }
        }
        TestSuite suite = createObject(TestSuite.class, suiteArgs);

        for(int i = 0; i < cmdLineArgs.length; i++) {
            if(cmdLineArgs[i].startsWith("--")) {
                String option = cmdLineArgs[i].substring(2);
                if(testClasses.containsKey(option) || publisherClasses.containsKey(option)) {
                    HashMap<String, String> args = new HashMap<>();
                    for(int j = i + 1; j < cmdLineArgs.length && !cmdLineArgs[j].startsWith("--"); j++) {
                        parseAndAddProperty(cmdLineArgs[j], args);
                        i = j;
                    }
                    if(testClasses.containsKey(option)) {
                        AbstractTest test = createObject(testClasses.get(option), args);
                        if(!args.containsKey("Weight"))
                            throw new IllegalArgumentException("Property Weight is required for class " + test.getClass().getSimpleName());
                        suite.add(test, Integer.parseInt(args.get("Weight")));
                    }
                    else if(publisherClasses.containsKey(option)) {
                        Publisher publisher = createObject(publisherClasses.get(option), args);
                        suite.addPublisher(publisher);
                    }
                }
            }
        }

        return suite;
    }

    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(120, "toughday","", options, "");
    }
}
