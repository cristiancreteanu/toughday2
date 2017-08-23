package com.adobe.qe.toughday.core.config;

import java.util.ArrayList;
import java.util.HashMap;
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

    private Map<String, String> globalParams = new HashMap<>();
    private Map<String, String> publishModeParams = new HashMap<>();
    private Map<String, String> runModeParams = new HashMap<>();
    private List<ClassMetaObject> itemsToAdd = new ArrayList<>();
    private List<NamedMetaObject> itemsToConfig = new ArrayList<>();
    private List<String> itemsToExclude = new ArrayList<>();

    public void setGlobalParams(Map<String, String> globalParams) {
        this.globalParams = globalParams;
    }

    public void setPublishModeParams(Map<String, String> publishModeParams) {
        this.publishModeParams = publishModeParams;
    }

    public void setRunModeParams(Map<String, String> runModeParams) {
        this.runModeParams = runModeParams;
    }

    public void configItem(String testName, Map<String, String> params) {
        itemsToConfig.add(new NamedMetaObject(testName, params));
    }

    public void addItem(String itemName, Map<String, String> params) {
        itemsToAdd.add(new ClassMetaObject(itemName, params));
    }

    public void exclude(String testName) {
        itemsToExclude.add(testName);
    }


    public Map<String, String> getGlobalParams(){
        return globalParams;
    }

    public Map<String, String> getPublishModeParams() { return publishModeParams; }

    public Map<String, String> getRunModeParams() { return runModeParams; }

    public List<String> getItemsToExclude() { return itemsToExclude; }

    public List<NamedMetaObject> getItemsToConfig() {
        return itemsToConfig;
    }

    public List<ClassMetaObject> getItemsToAdd() { return itemsToAdd;};

    public void merge(ConfigParams other) {

        globalParams.putAll(other.getGlobalParams());
        itemsToAdd.addAll(other.getItemsToAdd());
        itemsToExclude.addAll(other.getItemsToExclude());
        itemsToConfig.addAll(other.getItemsToConfig());

        if(other.runModeParams.containsKey("type"))
            this.runModeParams.clear();
        this.runModeParams.putAll(other.runModeParams);

        if(other.publishModeParams.containsKey("type"))
            this.publishModeParams.clear();
        this.publishModeParams.putAll(other.publishModeParams);
    }
}
