package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Computed average duration of all test executions. Formula: Sum (request time) / Runs  .")
public class Average extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getAverageDuration();
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
