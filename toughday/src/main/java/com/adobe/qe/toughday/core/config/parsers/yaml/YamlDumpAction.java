package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.Actions;
import java.util.HashMap;
import java.util.Map;

public abstract class YamlDumpAction {

    protected Map<String, Object> properties = new HashMap<>();
    protected String identifier;
    protected Actions action;

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return  properties;
    }
}