package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.Actions;

/**
 * Specifies how the exclude action is dumped when generating a yaml configuration file.
 */
public class YamlDumpExcludeAction extends YamlDumpAction {

    public YamlDumpExcludeAction(String identifier) {
        this.action = Actions.EXCLUDE;
        this.identifier = identifier;
    }

    public String getExclude() {
        return identifier;
    }
}
