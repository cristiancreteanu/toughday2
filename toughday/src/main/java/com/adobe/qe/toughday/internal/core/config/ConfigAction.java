package com.adobe.qe.toughday.internal.core.config;

import java.util.Map;

/**
 * Created by tuicu on 27/12/16.
 */
public interface ConfigAction {
    String value();
    void apply(ConfigParams configParams, String identifier, Map<String, Object> metaInfo);
    String actionParams();
    String actionDescription();
}