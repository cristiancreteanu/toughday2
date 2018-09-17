package com.adobe.qe.toughday.internal.core.config.parsers.yaml;

import com.adobe.qe.toughday.internal.core.config.Actions;
import com.adobe.qe.toughday.internal.core.config.ConfigParams;

import java.util.List;
import java.util.Map;

public class YamlParsePhase {
    private String name;
    private Boolean measurabile;
    private List<YamlParseAction> tests;
    private Map<String, Object> runmode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMeasurable() {
        return measurabile;
    }

    public void setMeasurable(Boolean measurable) {
        this.measurabile = measurable;
    }

    public List<YamlParseAction> getTests() {
        return tests;
    }

    public void setTests(List<YamlParseAction> tests) {
        this.tests = tests;
    }

    public Map<String, Object> getRunmode() {
        return runmode;
    }

    public void setRunmode(Map<String, Object> runmode) {
        this.runmode = runmode;
    }
}
