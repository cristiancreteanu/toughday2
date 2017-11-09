package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Number of runs divided by elapsed time. Formula: Runs / elapsed execution time.")
public class RealTP extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getRealThroughput();
    }

    @Override
    public String getFormat() {
        return "%." + this.getDecimals() + "f";
    }

    @Override
    public String getUnitOfMeasure() {
        return "rps";
    }
}
