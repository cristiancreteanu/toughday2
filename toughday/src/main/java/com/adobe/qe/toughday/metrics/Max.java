package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Highest duration of test execution.")
public class Max extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getMaxDuration();
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
