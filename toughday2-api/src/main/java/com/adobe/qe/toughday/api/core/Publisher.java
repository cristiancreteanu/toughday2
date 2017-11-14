package com.adobe.qe.toughday.api.core;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common interface for all publishers. Classes implementing this interface, if not abstract,
 * will be shown in help.
 */
public abstract class Publisher {
    private String name;
    private boolean rawPublish = true;
    private boolean aggregatedPublish = true;

    /**
     * Constructor
     */
    public Publisher() {
        this.name = getClass().getSimpleName();
    }

    /**
     * Getter for the name of the publisher.
     */
    @ConfigArgGet
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the publisher.
     */
    @ConfigArgSet(required = false, desc = "The name of this publisher")
    public void setName(String name) { this.name = name; }

    @ConfigArgSet(required = false, defaultValue = "true", desc = "Enable the raw result publishing")
    public void setRawPublish(String rawPublish) {
        this.rawPublish = Boolean.parseBoolean(rawPublish);
    }

    @ConfigArgGet
    public boolean getRawPublish() {
        return rawPublish;
    }

    @ConfigArgSet(required = false, defaultValue = "true", desc = "Enable the aggregated result publishing")
    public void setAggregatedPublish(String aggregatedPublish) {
        this.aggregatedPublish = Boolean.parseBoolean(aggregatedPublish);
    }

    @ConfigArgGet
    public boolean getAggregatedPublish() {
        return aggregatedPublish;
    }

    /**
     * Publish intermediate report
     * @param results
     */
    public void publishIntermediate(Map<String, List<MetricResult>> results) {
        if(aggregatedPublish) {
            doPublishIntermediate(results);
        }
    }

    /**
     * Publish final report
     * @param results
     */
    public void publishFinal(Map<String, List<MetricResult>> results) {
        if(aggregatedPublish) {
            doPublishFinal(results);
        }
    }

    public void publish(Collection<TestResult> testResults) {
        if(rawPublish) {
            doPublish(testResults);
        }
    }

    protected abstract void doPublishIntermediate(Map<String, List<MetricResult>> results);

    protected abstract void doPublishFinal(Map<String, List<MetricResult>> results);

    protected abstract void doPublish(Collection<TestResult> testResults);

    /**
     * Method that signals the publisher that it is stopped
     */
    public abstract void finish();
}
