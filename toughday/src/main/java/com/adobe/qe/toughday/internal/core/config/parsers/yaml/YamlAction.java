package com.adobe.qe.toughday.internal.core.config.parsers.yaml;

import com.adobe.qe.toughday.internal.core.config.Actions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuicu on 27/12/16.
 */
public class YamlAction {
    private Actions action;
    private String identifier;
    private Map<String, String> testMetaInfo = new HashMap<>();

    public void setAdd(String identifier) {
        this.action = Actions.ADD;
        this.identifier = identifier;
    }

    public void setConfig(String identifier) {
        this.action = Actions.CONFIG;
        this.identifier = identifier;
    }

    public void setExclude(String identifier) {
        this.action = Actions.EXCLUDE;
        this.identifier = identifier;
    }

    public void setProperties(Map<String, String> testMetaInfo) {
        this.testMetaInfo = testMetaInfo;
    }

    public Actions getAction() {
        return action;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getProperties() {
        return testMetaInfo;
    }

    @Override
    public String toString() {
        return action.value() + " " + identifier;
    }
}
