package com.adobe.qe.toughday.internal.core.config.parsers.yaml;

import com.adobe.qe.toughday.internal.core.config.Actions;

import java.util.Map;

/**
 *  Specifies how the config action is dumped when generating a yaml configuration file.
 */
public class YamlDumpConfigAction extends YamlDumpAction {

    public YamlDumpConfigAction(String identifier, Map<String, Object> properties) {
        this.action = Actions.CONFIG;
        this.identifier = identifier;
        this.properties = properties;
    }

    public String getConfig() {
        return identifier;
    }

}