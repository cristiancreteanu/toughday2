package com.adobe.qe.toughday.internal.core.config.parsers.yaml;

import java.util.List;
import java.util.Map;

public class YamlParsePhase {
    private String name;
    private Boolean measurabile;
    private String useconfig;
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

    public String getUseconfig() {
        return useconfig;
    }

    public void setUseconfig(String useconfig) {
        this.useconfig = useconfig;
    }
}
