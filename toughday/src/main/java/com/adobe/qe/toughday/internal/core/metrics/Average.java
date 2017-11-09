package com.adobe.qe.toughday.internal.core.metrics;


import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Computed average duration of all test executions. Formula: Sum (request time) / Runs  .")
public class Average extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getAverageDuration();
    }

    @Override
    public String getFormat() {
        return "%." + this.getDecimals() + "f";
    }

    @Override
    public String getUnitOfMeasure() {
        return "ms";
    }

}
