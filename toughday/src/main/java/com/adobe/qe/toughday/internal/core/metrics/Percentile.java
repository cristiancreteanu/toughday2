package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.RunMap;

@Description(desc = "Percentile.")
public class Percentile extends Metric {
    private double value;

    @ConfigArgSet(required = true, desc = "The value at which percentile will be calculated.")
    public Percentile setValue(String value) {
        this.value = Double.valueOf(value.substring(0,value.length() - 1));
        if (this.name.equals(getClass().getSimpleName())) {
            this.name = value + "p";
        }
        return this;
    }

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getValueAtPercentile(value);
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
