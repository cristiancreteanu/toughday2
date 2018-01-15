package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Number of fails.")
public class Failed extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getFailRuns();
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
