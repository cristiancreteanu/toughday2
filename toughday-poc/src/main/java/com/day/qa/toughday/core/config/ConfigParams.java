package com.day.qa.toughday.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 18/09/15.
 */
public class ConfigParams {
    public static class ParametrizedObject {
        private String className;
        private HashMap<String, String> parameters;

        public ParametrizedObject(String className, HashMap<String, String> parameters) {
            this.className = className;
            this.parameters = parameters;
        }

        public String getClassName() {
            return className;
        }

        public HashMap<String, String> getParameters() {
            return parameters;
        }
    }

    private HashMap<String, String> globalParams;
    private List<ParametrizedObject> tests = new ArrayList<>();
    private List<ParametrizedObject> publishers = new ArrayList<>();

    public void setGlobalParams(HashMap<String, String> globalParams) {
        this.globalParams = globalParams;
    }

    public void addTest(String testClassName, HashMap<String, String> params) {
        tests.add(new ParametrizedObject(testClassName, params));
    }

    public void addPublisher(String publisherClassName, HashMap<String, String> params) {
        publishers.add(new ParametrizedObject(publisherClassName, params));
    }

    public HashMap<String, String> getGlobalParams(){
        return globalParams;
    }

    public List<ParametrizedObject> getTests() {
        return tests;
    }

    public List<ParametrizedObject> getPublishers() {
        return publishers;
    }
}
