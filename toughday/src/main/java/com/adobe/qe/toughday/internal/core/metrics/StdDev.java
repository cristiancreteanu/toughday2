package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Standard deviation.")
public class StdDev extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getStandardDeviation();
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
