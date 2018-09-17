package com.adobe.qe.toughday.internal.core.engine;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.config.GlobalArgs;

public class Phase {
    private static final String DEFAULT_MEASURABILITY = "true";

    private String name;
    private boolean measurability;
    private String useconfig;
    private long duration;

    private TestSuite testSuite;
    private RunMode runMode;

    @ConfigArgGet
    public String getName() {
        return name;
    }

    @ConfigArgSet(required = false, desc = "The name of the phase.")
    public void setName(String name) {
        this.name = name;
    }

    @ConfigArgGet
    public boolean isMeasurability() {
        return measurability;
    }

    @ConfigArgSet(required = false, desc = "Option to specify whether the metrics of this phase will be taken into consideration",
    defaultValue = DEFAULT_MEASURABILITY)
    public void setMeasurability(String measurability) {
        this.measurability = Boolean.valueOf(measurability);
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
    public long getDuration() {
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
