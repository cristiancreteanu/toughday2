package com.adobe.qe.toughday.core;

import java.util.Collection;

/**
 * Common interface for all publishers. Classes implementing this interface, if not abstract,
 * will be shown in help.
 */
public interface Publisher {

    /**
     * Publish intermediate report
     * @param testStatistics
     */
    void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics);

    /**
     * Publish final report
     * @param testStatistics
     */
    void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics);
}
