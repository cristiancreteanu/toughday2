package com.adobe.qe.toughday.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An object that has the configuration params parsed, but still in String form.
 * What a ConfigurationParser returns and is used by Configuration to instantiate all concrete objects.
 */
public class ConfigParams {
    public static class MetaObject {
        private Map<String, String> parameters;

        public MetaObject(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }
    }

    public static class ClassMetaObject extends MetaObject {
        private String className;

        public ClassMetaObject(String className, Map<String, String> parameters) {
            super(parameters);
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class NamedMetaObject extends MetaObject {
        private String name;

        public NamedMetaObject(String name, Map<String, String> parameters) {
            super(parameters);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Map<String, String> globalParams;
    private List<ClassMetaObject> testsToAdd = new ArrayList<>();
    private List<ClassMetaObject> publishers = new ArrayList<>();
    private List<NamedMetaObject> testsToConfig = new ArrayList<>();
    private List<String> testsToExclude = new ArrayList<>();

    public void setGlobalParams(Map<String, String> globalParams) {
        this.globalParams = globalParams;
    }

    public void addTest(String testClassName, Map<String, String> params) {
        testsToAdd.add(new ClassMetaObject(testClassName, params));
    }

    public void configTest(String testName, Map<String, String> params) {
        testsToConfig.add(new NamedMetaObject(testName, params));
    }

    public void excludeTest(String testName) {
        testsToExclude.add(testName);
    }

    public void addPublisher(String publisherClassName, Map<String, String> params) {
        publishers.add(new ClassMetaObject(publisherClassName, params));
    }

    public Map<String, String> getGlobalParams(){
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
