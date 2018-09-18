package com.adobe.qe.toughday.internal.core.engine;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.config.GlobalArgs;

import java.util.Map;

public class Phase {
    private static final String DEFAULT_MEASURABILITY = "true";

    private String name;
    private Boolean measurable;
    private String useconfig;
    private Long duration;

    private TestSuite testSuite;
    private RunMode runMode;

    public Phase(Map<String, Object> properties, TestSuite testSuite, RunMode runMode) {
        name = properties.containsKey("name") ? properties.get("name").toString() : "";
        measurable = properties.containsKey("measurable") ? Boolean.valueOf(properties.get("measurable").toString()) : null;
        useconfig = properties.containsKey("useconfig") ? properties.get("useconfig").toString() : "";
        duration = properties.containsKey("duration") ? GlobalArgs.parseDurationToSeconds(properties.get("duration").toString()) : null;

        this.testSuite = testSuite;
        this.runMode = runMode;
    }

    @ConfigArgGet
    public String getName() {
        return name;
    }

    @ConfigArgSet(required = false, desc = "The name of the phase.")
    public void setName(String name) {
        this.name = name;
    }

    @ConfigArgGet
    public boolean getMeasurable() {
        return measurable;
    }

    @ConfigArgSet(required = false, desc = "Option to specify whether the metrics of this phase will be taken into consideration",
        defaultValue = DEFAULT_MEASURABILITY)
    public void setMeasurable(String measurabile) {
        this.measurable = Boolean.valueOf(measurabile);
    }

    @ConfigArgGet
    public String getUseconfig() {
        return useconfig;
    }

    @ConfigArgSet(required = false, desc = "The name of the phase from which to import the configuration.")
    public void setUseconfig(String useconfig) {
        this.useconfig = useconfig;
    }

    @ConfigArgGet
    public Long getDuration() {
        return duration;
    }

    @ConfigArgSet(required = false, desc = "The duration of the current phase.", defaultValue = GlobalArgs.DEFAULT_DURATION)
    public void setDuration(String duration) {
        this.duration = GlobalArgs.parseDurationToSeconds(duration);
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public RunMode getRunMode() {
        return runMode;
    }
}
