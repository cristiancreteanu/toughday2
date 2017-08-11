package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.annotations.Internal;
import com.adobe.qe.toughday.core.engine.RunMode;
import com.adobe.qe.toughday.core.engine.PublishMode;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Wrapper for the reflections library. Uses singleton.
 */
public class ReflectionsContainer {
    private static final Pattern toughdayContentPackagePattern = Pattern.compile("toughday_sample-.*.zip");
    private static Reflections reflections = new Reflections("com.adobe.qe");
    private static ReflectionsContainer instance = new ReflectionsContainer();

    /**
     * Getter for the container.
     */
    public static ReflectionsContainer getInstance() {
        return instance;
    }

    private HashMap<String, Class<? extends AbstractTest>> testClasses;
    private HashMap<String, Class<? extends Publisher>> publisherClasses;
    private HashMap<String, Class<? extends SuiteSetup>> suiteSetupClasses;
    private HashMap<String, Class<? extends PublishMode>> publishModeClasses;
    private HashMap<String, Class<? extends RunMode>> runModeClasses;

    private String toughdayContentPackage;

    private static boolean excludeClass(Class klass) {
        return Modifier.isAbstract(klass.getModifiers())
                || !Modifier.isPublic(klass.getModifiers())
                || klass.isAnnotationPresent(Internal.class);
    }

    /**
     * Constructor.
     */

    private ReflectionsContainer() {

        updateContainerContent();

        Reflections reflections = new Reflections("", new ResourcesScanner());
        Iterator<String> iterator = reflections.getResources(toughdayContentPackagePattern).iterator();
        if (iterator.hasNext()) {
            toughdayContentPackage = iterator.next();
        }
    }

    private void updateContainerContent() {

        testClasses = new HashMap<>();
        publisherClasses = new HashMap<>();
        suiteSetupClasses = new HashMap<>();
        publishModeClasses = new HashMap<>();
        runModeClasses = new HashMap<>();


        for(Class<? extends AbstractTest> testClass : reflections.getSubTypesOf(AbstractTest.class)) {
            if(excludeClass(testClass))
                continue;
            if(testClasses.containsKey(testClass.getSimpleName()))
                throw new IllegalStateException("A test class with this name already exists here: "
                        + testClasses.get(testClass.getSimpleName()).getName());
            testClasses.put(testClass.getSimpleName(), testClass);
        }

        for (Class<? extends Publisher> publisherClass : reflections.getSubTypesOf(Publisher.class)) {
            if (excludeClass(publisherClass))
                continue;
            if (publisherClasses.containsKey(publisherClass.getSimpleName()))
                throw new IllegalStateException("A publisher class with this name already exists here: "
                        + publisherClasses.get(publisherClass.getSimpleName()).getName());
            publisherClasses.put(publisherClass.getSimpleName(), publisherClass);
        }

        for (Class<? extends SuiteSetup> suiteSetupClass : reflections.getSubTypesOf(SuiteSetup.class)) {
            if (Modifier.isAbstract(suiteSetupClass.getModifiers()))
                continue;
            if (suiteSetupClasses.containsKey(suiteSetupClass.getSimpleName()))
                throw new IllegalStateException("A suite class with this name already exists here: "
                        + suiteSetupClasses.get(suiteSetupClass.getSimpleName()).getName());
            suiteSetupClasses.put(suiteSetupClass.getSimpleName(), suiteSetupClass);
        }

        for (Class<? extends PublishMode> publishModeClass : reflections.getSubTypesOf(PublishMode.class)) {
            if(excludeClass(publishModeClass)) continue;
            String identifier = publishModeClass.getSimpleName().toLowerCase();
            if(publishModeClasses.containsKey(identifier)) {
                throw new IllegalStateException("A publish mode class with this name already exists here: "
                        + publishModeClasses.get(identifier).getName());
            }
            publishModeClasses.put(identifier, publishModeClass);
        }

        for(Class<? extends RunMode> runModeClass : reflections.getSubTypesOf(RunMode.class)) {
            if(excludeClass(runModeClass)) continue;
            String identifier = runModeClass.getSimpleName().toLowerCase();
            if(runModeClasses.containsKey(identifier)) {
                throw new IllegalStateException("A run mode class with this name already exists here: " +
                        runModeClasses.get(identifier).getName());
            }
            runModeClasses.put(identifier, runModeClass);
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

    /**
     * Getter for the map of PublishMode classes
     */
    public Map<String, Class<? extends PublishMode>> getPublishModeClasses() { return publishModeClasses; }

    public String getToughdayContentPackage() {
        return toughdayContentPackage;
    }

    public HashMap<String,Class<? extends RunMode>> getRunModeClasses() {
        return runModeClasses;
    }

    public boolean containsClass(String className) {
        return testClasses.containsKey(className) || publisherClasses.containsKey(className)
                || publishModeClasses.containsKey(className) || runModeClasses.containsKey(className)
                || suiteSetupClasses.containsKey(className);
    }

    /**
     * This method makes the Reflections instance aware about the new classes dynamically loaded from the jar files.
     * @param reflections
     */

    public void merge(Reflections reflections) {
        this.reflections = reflections;
        updateContainerContent();
    }

    public static <T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type) {
       return reflections.getSubTypesOf(type);
    }

}
