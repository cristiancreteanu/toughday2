package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Number of successful runs.")
public class Passed extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getTotalRuns();
    }

    @Override
    public String getFormat() {
        return "%d";
    }

    @Override
    public String getUnitOfMeasure() {
        return "";
    }

}
