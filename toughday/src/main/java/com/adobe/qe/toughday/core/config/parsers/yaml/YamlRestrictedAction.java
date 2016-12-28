package com.adobe.qe.toughday.core.config.parsers.yaml;

/**
 * For now used to restrict actions on publishers to allow just "add"
 */
public class YamlRestrictedAction extends YamlAction {

    @Override
    public void setConfig(String identifier) {
        throw new UnsupportedOperationException("Cannot config publishers");
    }

    @Override
    public void setExclude(String identifier) {
        throw new UnsupportedOperationException("Cannot exclude publishers");
    }

}
