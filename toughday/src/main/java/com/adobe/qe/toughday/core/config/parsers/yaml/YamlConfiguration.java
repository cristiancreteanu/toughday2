package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.ConfigParams;

import java.util.List;
import java.util.Map;

/**
 * Created by tuicu on 28/12/16.
 */
public class YamlConfiguration {

    private ConfigParams configParams = new ConfigParams();

    public void setGlobals(Map<String, Object> globals) {
        this.configParams.setGlobalParams(globals);
    }

    public void setPublishmode(Map<String, Object> publishModeProperties) {
        configParams.setPublishModeParams(publishModeProperties);
    }

    public void setRunmode(Map<String, Object> runmode) {
        configParams.setRunModeParams(runmode);
    }

    public void setTests(List<YamlParseAction> tests) {
        for(YamlParseAction yamlParseAction : tests) {
            yamlParseAction.getAction().apply(configParams, yamlParseAction.getIdentifier(), yamlParseAction.getProperties());
        }
    }

    public void setPublishers(List<YamlParseAction> publishers) {
        for(YamlParseAction yamlParseAction : publishers) {
            yamlParseAction.getAction().apply(configParams, yamlParseAction.getIdentifier(), yamlParseAction.getProperties());
        }
    }

    public void setMetrics(List<YamlParseAction> metrics) {
        for (YamlParseAction yamlParseAction : metrics) {
            yamlParseAction.getAction().apply(configParams, yamlParseAction.getIdentifier(), yamlParseAction.getProperties());
        }
    }

    public void setExtensions(List<YamlParseAction> extensions) {
        for (YamlParseAction yamlAction : extensions) {
            yamlAction.getAction().apply(configParams, yamlAction.getIdentifier(), yamlAction.getProperties());
        }
    }

    public ConfigParams getConfigParams() {
        return configParams;
    }
}
