package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.Actions;
import java.util.Map;

/**
 * Specifies how the add action is dumped when generating a yaml configuration file.
 */
public class YamlDumpAddAction extends YamlDumpAction {

    public YamlDumpAddAction(String identifier, Map<String, Object> properties) {
        this.action = Actions.ADD;
        this.identifier = identifier;
        this.properties = properties;
    }

    public String getAdd() {
        return identifier;
    }

}