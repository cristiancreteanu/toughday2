package com.adobe.qe.toughday.core.config.parsers.yaml;

import com.adobe.qe.toughday.core.config.ConfigParams;

import java.util.List;
import java.util.Map;

/**
 * Created by tuicu on 28/12/16.
 */
public class YamlConfiguration {

    private ConfigParams configParams = new ConfigParams();

    public void setGlobals(Map<String, String> globals) {
        this.configParams.setGlobalParams(globals);
    }

    public void setPublishmode(Map<String, String> publishModeProperties) {
        configParams.setPublishModeParams(publishModeProperties);
    }

    public void setRunmode(Map<String, String> runmode) {
        configParams.setRunModeParams(runmode);
    }

    public void setTests(List<YamlAction> tests) {
        for(YamlAction yamlAction : tests) {
            yamlAction.getAction().apply(configParams, yamlAction.getIdentifier(), yamlAction.getProperties());
        }
    }

    public void setPublishers(List<YamlAction> publishers) {
        for(YamlAction yamlAction : publishers) {
            yamlAction.getAction().apply(configParams, yamlAction.getIdentifier(), yamlAction.getProperties());
        }
    }

    public void setMetrics(List<YamlAction> metrics) {
        for(YamlAction yamlAction : metrics) {
            yamlAction.getAction().apply(configParams, yamlAction.getIdentifier(), yamlAction.getProperties());
        }
    }

    public void setExtensions(List<YamlAction> extensions) {
        for (YamlAction yamlAction : extensions) {
            yamlAction.getAction().apply(configParams, yamlAction.getIdentifier(), yamlAction.getProperties());
        }
    }

    public ConfigParams getConfigParams() {
        return configParams;
    }
}
