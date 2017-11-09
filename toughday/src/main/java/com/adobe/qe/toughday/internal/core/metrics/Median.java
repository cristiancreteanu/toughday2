package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Computed median duration of all test executions.")
public class Median extends Metric{

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getMedianDuration();
    }

    @Override
    public String getFormat() {
        return "%d";
    }

    @Override
    public String getUnitOfMeasure() {
        return "ms";
    }

}
