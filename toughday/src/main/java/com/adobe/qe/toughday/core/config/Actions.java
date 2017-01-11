package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.ReflectionsContainer;

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
        public void apply(ConfigParams configParams, String identifier, Map<String, String> metaInfo) {
            if (ReflectionsContainer.getInstance().getTestClasses().containsKey(identifier)) {
                configParams.addTest(identifier, metaInfo);
            } else if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                configParams.addPublisher(identifier, metaInfo);
            } else {
                throw new IllegalArgumentException("Unknown publisher or test class: " + identifier);
            }
        }

        @Override
        public String actionParams() {
            return "TestClass/PublisherClass property1=val property2=val";
        }

        @Override
        public String actionDescription() {
            return "Add a test to the suite or a publisher";
        }
    },
    CONFIG {
        @Override
        public String value() {
            return CONFIG_ACTION;
        }

        @Override
        public void apply(ConfigParams configParams, String identifier, Map<String, String> metaInfo) {
            configParams.configTest(identifier, metaInfo);
        }

        @Override
        public String actionParams() {
            return "TestName property1=val property2=val";
        }

        @Override
        public String actionDescription() {
            return "Override parameters for a test from config file or a predefined suite";
        }
    },
    EXCLUDE {
        @Override
        public String value() {
            return EXCLUDE_ACTION;
        }

        @Override
        public void apply(ConfigParams configParams, String identifier, Map<String, String> metaInfo) {
            if (metaInfo != null && metaInfo.size() != 0) {
                throw new IllegalArgumentException("--exclude cannot have properties for identifier: " + identifier);
            }

            if (ReflectionsContainer.getInstance().getPublisherClasses().containsKey(identifier)) {
                //TODO exclude publishers
            } else {
                configParams.excludeTest(identifier);
            }
        }

        @Override
        public String actionParams() {
            return "TestName";
        }

        @Override
        public String actionDescription() {
            return "Exclude a test from config file or a predefined suite";
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