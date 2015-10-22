package com.adobe.qe.toughday.core;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Wrapper for the reflections library. Uses singleton.
 */
public class ReflectionsContainer {
    private static Reflections reflections = new Reflections("com.adobe.qe");
    private static ReflectionsContainer instance = new ReflectionsContainer();

    /**
     * Getter for the container.
     */
    public static ReflectionsContainer getInstance() {
        return instance;
    }

    /**
     * Getter for the underlining instance of the Reflections object.
     */
    public static Reflections getReflections() {
        return reflections;
    }

    private HashMap<String, Class<? extends AbstractTest>> testClasses;
    private HashMap<String, Class<? extends Publisher>> publisherClasses;
    private HashMap<String, Class<? extends SuiteSetup>> suiteSetupClasses;

    /**
     * Constructor.
     */
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
            testClasses.put(testClass.getName(), testClass);
        }

        for (Class<? extends Publisher> publisherClass : reflections.getSubTypesOf(Publisher.class)) {
            if (Modifier.isAbstract(publisherClass.getModifiers()))
                continue;
            if (publisherClasses.containsKey(publisherClass.getSimpleName()))
                throw new IllegalStateException("A publisher class with this name already exists here: "
                        + publisherClasses.get(publisherClass.getSimpleName()).getName());
            publisherClasses.put(publisherClass.getSimpleName(), publisherClass);
            publisherClasses.put(publisherClass.getName(), publisherClass);
        }

        for (Class<? extends SuiteSetup> suiteSetupClass : reflections.getSubTypesOf(SuiteSetup.class)) {
            if (Modifier.isAbstract(suiteSetupClass.getModifiers()))
                continue;
            if (suiteSetupClasses.containsKey(suiteSetupClass.getSimpleName()))
                throw new IllegalStateException("A suite class with this name already exists here: "
                        + suiteSetupClasses.get(suiteSetupClass.getSimpleName()).getName());
            suiteSetupClasses.put(suiteSetupClass.getSimpleName(), suiteSetupClass);
            suiteSetupClasses.put(suiteSetupClass.getName(), suiteSetupClass);
        }

    }

    /**
     * Getter for the map of test classes.
     */
    public HashMap<String, Class<? extends AbstractTest>> getTestClasses() {
        return testClasses;
    }

    /**
     * Getter for the map of publisher classes.
     */
    public HashMap<String, Class<? extends Publisher>> getPublisherClasses() {
        return publisherClasses;
    }

    /**
     * Getter for the map of SuiteSetup classes.
     */
    public HashMap<String, Class<? extends SuiteSetup>> getSuiteSetupClasses() {
        return suiteSetupClasses;
    }
}
