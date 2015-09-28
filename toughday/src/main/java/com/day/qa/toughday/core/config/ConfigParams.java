package com.day.qa.toughday.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 18/09/15.
 * An object that has the configuration params parsed, but still in String form.
 * What a ConfigurationParser returns and is used by Configuration to instantiate all concrete objects.
 */
public class ConfigParams {
    public static class MetaObject {
        private HashMap<String, String> parameters;

        public MetaObject(HashMap<String, String> parameters) {
            this.parameters = parameters;
        }

        public HashMap<String, String> getParameters() {
            return parameters;
        }
    }

    public static class ClassMetaObject extends MetaObject {
        private String className;

        public ClassMetaObject(String className, HashMap<String, String> parameters) {
            super(parameters);
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class NamedMetaObject extends MetaObject {
        private String name;

        public NamedMetaObject(String name, HashMap<String, String> parameters) {
            super(parameters);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private HashMap<String, String> globalParams;
    private List<ClassMetaObject> testsToAdd = new ArrayList<>();
    private List<ClassMetaObject> publishers = new ArrayList<>();
    private List<NamedMetaObject> testsToConfig = new ArrayList<>();
    private List<String> testsToExclude = new ArrayList<>();

    public void setGlobalParams(HashMap<String, String> globalParams) {
        this.globalParams = globalParams;
    }

    public void addTest(String testClassName, HashMap<String, String> params) {
        testsToAdd.add(new ClassMetaObject(testClassName, params));
    }

    public void configTest(String testName, HashMap<String, String> params) {
        testsToConfig.add(new NamedMetaObject(testName, params));
    }

    public void excludeTest(String testName) {
        testsToExclude.add(testName);
    }

    public void addPublisher(String publisherClassName, HashMap<String, String> params) {
        publishers.add(new ClassMetaObject(publisherClassName, params));
    }

    public HashMap<String, String> getGlobalParams(){
        return globalParams;
    }

    public List<ClassMetaObject> getTestsToAdd() {
        return testsToAdd;
    }

    public List<String> getTestsToExclude() { return testsToExclude; }

    public List<NamedMetaObject> getTestsToConfig() {
        return testsToConfig;
    }

    public List<ClassMetaObject> getPublishers() {
        return publishers;
    }
}
