package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Number of successful runs.")
public class Passed extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getTotalRuns();
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
