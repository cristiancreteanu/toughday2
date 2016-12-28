package com.adobe.qe.toughday.core.config;

import java.util.Map;

/**
 * Created by tuicu on 27/12/16.
 */
public interface ConfigAction {
    String value();
    void apply(ConfigParams configParams, String identifier, Map<String, String> metaInfo);
    String actionParams();
    String actionDescription();
}