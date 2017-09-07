package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Computed median duration of all test executions.")
public class Median extends Metric{

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getMedianDuration();
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
