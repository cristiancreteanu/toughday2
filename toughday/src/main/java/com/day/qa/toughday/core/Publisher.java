package com.day.qa.toughday.core;

import java.util.Collection;

/**
 * Created by tuicu on 21/08/15.
 * Common interface for all publishers. Classes implementing this interface, if not abstract,
 * will be shown in help.
 */
public interface Publisher {
    void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics);
    void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics);
}
