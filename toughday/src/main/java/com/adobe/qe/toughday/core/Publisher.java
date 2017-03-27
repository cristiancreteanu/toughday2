package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;

import java.util.Collection;

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
     * @param testStatistics
     */
    public abstract void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics);

    /**
     * Publish final report
     * @param testStatistics
     */
    public abstract void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics);

    /**
     * Method that signals the publisher that it is stopped
     */
    public abstract void finish();
}
