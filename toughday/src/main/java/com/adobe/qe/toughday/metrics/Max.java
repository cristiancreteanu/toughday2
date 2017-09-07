package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Highest duration of test execution.")
public class Max extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getMaxDuration();
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
