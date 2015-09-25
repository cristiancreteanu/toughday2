package com.day.qa.toughday.core;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by tuicu on 18/09/15.
 */
public class ReflectionsContainer {
    private static Reflections reflections = new Reflections("com.day.qa");
    private static ReflectionsContainer instance = new ReflectionsContainer();


    public static ReflectionsContainer getInstance() {
        return instance;
    }

    public static Reflections getReflections() {
        return reflections;
    }


    private HashMap<String, Class<? extends AbstractTest>> testClasses;
    private HashMap<String, Class<? extends Publisher>> publisherClasses;
    private HashMap<String, Class<? extends SuiteSetup>> suiteSetupClasses;

    private ReflectionsContainer() {
        testClasses = new HashMap<>();
        publisherClasses = new HashMap<>();
        suiteSetupClasses = new HashMap<>();

        for(Class<? extends AbstractTest> testClass : reflections.getSubTypesOf(AbstractTest.class)) {
            if(Modifier.isAbstract(testClass.getModifiers()))
                continue;
            if(testClasses.containsKey(testClass.getSimpleName()))
                throw new IllegalStateException("A test class with this name already exists here: "
                        + testClasses.get(testClass.getSimpleName()).getName());
            testClasses.put(testClass.getSimpleName(), testClass);
        }

        for(Class<? extends Publisher> publisherClass : reflections.getSubTypesOf(Publisher.class)) {
            if(Modifier.isAbstract(publisherClass.getModifiers()))
                continue;
            if(publisherClasses.containsKey(publisherClass.getSimpleName()))
                throw new IllegalStateException("A publisher class with this name already exists here: "
                        + publisherClasses.get(publisherClass.getSimpleName()).getName());
            publisherClasses.put(publisherClass.getSimpleName(), publisherClass);
        }

        for(Class<? extends SuiteSetup> suiteSetupClass : reflections.getSubTypesOf(SuiteSetup.class)) {
            if(Modifier.isAbstract(suiteSetupClass.getModifiers()))
                continue;
            if(suiteSetupClasses.containsKey(suiteSetupClass.getSimpleName()))
                throw new IllegalStateException("A suite class with this name already exists here: "
                        + suiteSetupClasses.get(suiteSetupClass.getSimpleName()).getName());
            suiteSetupClasses.put(suiteSetupClass.getSimpleName(), suiteSetupClass);
        }

    }

    public HashMap<String, Class<? extends AbstractTest>> getTestClasses() {
        return testClasses;
    }

    public HashMap<String, Class<? extends Publisher>> getPublisherClasses() {
        return publisherClasses;
    }

    public HashMap<String, Class<? extends SuiteSetup>> getSuiteSetupClasses() {
        return suiteSetupClasses;
    }
}
