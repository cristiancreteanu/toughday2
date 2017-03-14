package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.annotations.Internal;
import com.adobe.qe.toughday.core.engine.publishmodes.PublishMode;
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

    /**
     * Getter for the underlining instance of the Reflections object.
     */
    public static Reflections getReflections() {
        return reflections;
    }

    private HashMap<String, Class<? extends AbstractTest>> testClasses;
    private HashMap<String, Class<? extends Publisher>> publisherClasses;
    private HashMap<String, Class<? extends SuiteSetup>> suiteSetupClasses;
    private HashMap<String, Class<? extends PublishMode>> publishModeClasses;

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
        testClasses = new HashMap<>();
        publisherClasses = new HashMap<>();
        suiteSetupClasses = new HashMap<>();
        publishModeClasses = new HashMap<>();

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

        Reflections reflections = new Reflections("", new ResourcesScanner());

        Iterator<String> iterator = reflections.getResources(toughdayContentPackagePattern).iterator();
        if (iterator.hasNext()) {
            toughdayContentPackage = iterator.next();
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
}
