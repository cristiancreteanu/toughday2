package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Number of skipped runs.")
public class Skipped extends Metric {
    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getSkippedRuns();
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
