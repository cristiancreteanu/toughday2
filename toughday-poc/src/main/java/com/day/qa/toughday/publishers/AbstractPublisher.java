package com.day.qa.toughday.publishers;

import com.day.qa.toughday.RunMap;

import java.util.Collection;

/**
 * Created by tuicu on 21/08/15.
 */
public abstract class AbstractPublisher {
    public abstract void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics);
    public abstract void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics);
}
