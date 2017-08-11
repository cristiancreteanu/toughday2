package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.metrics.ResultInfo;

import java.util.List;
import java.util.Map;

/**
 * Common interface for all publishers. Classes implementing this interface, if not abstract,
 * will be shown in help.
 */
public abstract class Publisher {
    private String name;

    public Publisher() {
        this.name = getClass().getSimpleName();
    }

    @ConfigArgGet
    public String getName() {
        return name;
    }

    @ConfigArgSet(required = false, defaultValue = "The class name", desc = "The name of this publisher")
    public void setName(String name) { this.name = name; }

    /**
     * Publish intermediate report
     * @param testsResults
     */
    public abstract void publishIntermediate(Map<String, List<ResultInfo>> testsResults);

    /**
     * Publish final report
     * @param testsResults
     */
    public abstract void publishFinal(Map<String, List<ResultInfo>> testsResults);

    /**
     * Method that signals the publisher that it is stopped
     */
    public abstract void finish();
}
