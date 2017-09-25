package com.adobe.qe.toughday.core.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that has the configuration params parsed, but still in String form.
 * What a ConfigurationParser returns and is used by Configuration to instantiate all concrete objects.
 */
public class ConfigParams implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(ConfigParams.class);

    public static class MetaObject  implements Serializable {
        private Map<String, Object> parameters;

        public MetaObject(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }
    }

    public static class ClassMetaObject extends MetaObject {
        private String className;

        public ClassMetaObject(String className, Map<String, Object> parameters) {
            super(parameters);
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class NamedMetaObject extends MetaObject {
        private String name;

        public NamedMetaObject(String name, Map<String, Object> parameters) {
            super(parameters);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /** Creates a copy of the object.
     * @param object
     * @return
     */
    public static<T> T deepClone(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
            oos.writeObject(object);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
            return (T) ois.readObject();
        }
        catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    private Map<String, Object> globalParams = new HashMap<>();
    private Map<String, Object> publishModeParams = new HashMap<>();
    private Map<String, Object> runModeParams = new HashMap<>();
    private List<ClassMetaObject> itemsToAdd = new ArrayList<>();
    private List<NamedMetaObject> itemsToConfig = new ArrayList<>();
    private List<String> itemsToExclude = new ArrayList<>();

    public void setGlobalParams(Map<String, Object> globalParams) {
        this.globalParams = globalParams;
    }

    public void setPublishModeParams(Map<String, Object> publishModeParams) {
        this.publishModeParams = publishModeParams;
    }

    public void setRunModeParams(Map<String, Object> runModeParams) {
        this.runModeParams = runModeParams;
    }

    public void configItem(String testName, Map<String, Object> params) {
        itemsToConfig.add(new NamedMetaObject(testName, params));
    }

    public void addItem(String itemName, Map<String, Object> params) {
        itemsToAdd.add(new ClassMetaObject(itemName, params));
    }

    public void exclude(String testName) {
        itemsToExclude.add(testName);
    }


    public Map<String, Object> getGlobalParams(){
        return globalParams;
    }

    public Map<String, Object> getPublishModeParams() { return publishModeParams; }

    public Map<String, Object> getRunModeParams() { return runModeParams; }

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
