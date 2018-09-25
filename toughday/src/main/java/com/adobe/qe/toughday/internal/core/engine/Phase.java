package com.adobe.qe.toughday.internal.core.engine;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.config.GlobalArgs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Phase {
    private static final String DEFAULT_MEASURABILITY = "true";

    private String name;
    private Boolean measurable;
    private String useconfig;
    private Long duration;

    private TestSuite testSuite;
    private RunMode runMode;
    private PublishMode publishMode;
    private Map<AbstractTest, AtomicLong> counts = new HashMap<>();

    @ConfigArgGet
    public String getName() {
        return name;
    }

    @ConfigArgSet(required = false, desc = "The name of the phase.")
    public void setName(String name) {
        this.name = name;
    }

    @ConfigArgGet
    public Boolean getMeasurable() {
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

    @ConfigArgSet(required = false, desc = "The duration of the current phase.")
    public void setDuration(String duration) {
        if (duration != null) {
            this.duration = GlobalArgs.parseDurationToSeconds(duration);
        }
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public PublishMode getPublishMode() {
        return publishMode;
    }

    public Map<AbstractTest, AtomicLong> getCounts() {
        return counts;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public void setRunMode(RunMode runMode) {
        this.runMode = runMode;
    }

    public void setPublishMode(PublishMode publishMode) {
        this.publishMode = publishMode;
    }
}
