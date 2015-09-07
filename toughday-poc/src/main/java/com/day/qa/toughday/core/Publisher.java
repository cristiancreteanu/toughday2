package com.day.qa.toughday.core;

import com.day.qa.toughday.core.RunMap;

import java.util.Collection;

/**
 * Created by tuicu on 21/08/15.
 */
public interface Publisher {
    void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics);
    void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics);
}
