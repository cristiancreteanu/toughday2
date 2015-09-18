package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by tuicu on 18/09/15.
 */
public class ConfigurationManager {

    public static String propertyFromMethod(String methodName) {
        return methodName.startsWith("set") ? methodName.substring(3) : methodName;
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
            method.invoke(testObject, value);
        }
        return testObject;
    }

    private ConfigurationParser getConfigurationParser(String[] args) {
        //Insert logic here to select from other types of parsers
        return new CliParser();
    }

    public TestSuite createTestSuite(String[] args)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ConfigurationParser parser = getConfigurationParser(args);
        ConfigParams configParams= parser.parse(args);

        HashMap<String, String> globalArgs = configParams.getGlobalParams();

        GlobalArgs globalArgsObject = createObject(GlobalArgs.class, globalArgs);
        GlobalArgs.setInstance(globalArgsObject);

        TestSuite suite = createObject(TestSuite.class, globalArgs);

        for(ConfigParams.ParametrizedObject testMeta : configParams.getTests()) {
            AbstractTest test = createObject(
                    ReflectionsContainer.getInstance().getTestClasses().get(testMeta.getClassName()),
                    testMeta.getParameters());
            if(!testMeta.getParameters().containsKey("Weight"))
                throw new IllegalArgumentException("Property Weight is required for class " + test.getClass().getSimpleName());
            suite.add(test, Integer.parseInt(testMeta.getParameters().get("Weight")));
        }

        for(ConfigParams.ParametrizedObject publisherMeta : configParams.getPublishers()) {
            Publisher publisher = createObject(
                    ReflectionsContainer.getInstance().getPublisherClasses().get(publisherMeta.getClassName()),
                    publisherMeta.getParameters());
            suite.addPublisher(publisher);
        }

        return suite;
    }

    public void printHelp() {
        CliParser cliParser = new CliParser();
        cliParser.printHelp();
    }

}
