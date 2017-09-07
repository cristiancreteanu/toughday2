package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;

public class Skipped extends Metric {
    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getSkippedRuns();
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
