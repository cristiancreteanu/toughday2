package com.adobe.qe.toughday.internal.core.config;

import com.adobe.qe.toughday.internal.core.ReflectionsContainer;

import java.util.Map;

/**
 * Created by tuicu on 27/12/16.
 */
public enum Actions implements ConfigAction {
    ADD {
        @Override
        public String value() {
            return ADD_ACTION;
        }

        @Override
        public void apply(ConfigParams configParams, String identifier, Map<String, Object> metaInfo) {
            configParams.addItem(identifier, metaInfo);
        }

        @Override
        public String actionParams() {
            return "ExtensionJar | [f.q.d.n]TestClass/[f.q.d.n]PublisherClass/[f.q.d.n]MetricClass property1=val property2=val";
        }

        @Override
        public String actionDescription() {
            return "Add an extension or a test to the suite or a publisher/metric. For test, publisher and metric it can be used the fully qualified domain name which represents the package's name.";
        }
    },
    CONFIG {
        @Override
        public String value() {
            return CONFIG_ACTION;
        }

        @Override
        public void apply(ConfigParams configParams, String identifier, Map<String, Object> metaInfo) {
            configParams.configItem(identifier, metaInfo);
        }

        @Override
        public String actionParams() {
            return "TestName/PublisherName/MetricName property1=val property2=val";
        }

        @Override
        public String actionDescription() {
            return "Override parameters for a test/publisher/metric from config file or a predefined suite";
        }
    },
    EXCLUDE {
        @Override
        public String value() {
            return EXCLUDE_ACTION;
        }

        @Override
        public void apply(ConfigParams configParams, String identifier, Map<String, Object> metaInfo) {
            if (metaInfo != null && metaInfo.size() != 0) {
                throw new IllegalArgumentException("--exclude cannot have properties for identifier: " + identifier);
            }

            configParams.exclude(identifier);
        }

        @Override
        public String actionParams() {
            return "TestName/PublisherName/MetricName";
        }

        @Override
        public String actionDescription() {
            return "Exclude a test/publisher/metric from config file or a predefined suite.";
        }
    };

    public static Actions fromString(String actionString) {
        for(Actions action : Actions.values()) {
            if (action.value().equals(actionString))
                return action;
        }
        throw new IllegalStateException("There's no \"" + actionString + "\" action");
    }

    public static boolean isAction(String actionString) {
        for(Actions actions : Actions.values()) {
            if (actions.value().equals(actionString)) {
                return true;
            }
        }

        return false;
    }

    private static final String ADD_ACTION = "add";
    private static final String CONFIG_ACTION = "config";
    private static final String EXCLUDE_ACTION = "exclude";
}