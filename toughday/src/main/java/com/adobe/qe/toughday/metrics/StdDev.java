package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Standard deviation.")
public class StdDev extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getStandardDeviation();
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
