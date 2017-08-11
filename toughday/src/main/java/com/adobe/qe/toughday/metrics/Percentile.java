package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgSet;

@Description(desc = "Percentile.")
public class Percentile extends Metric {
    private double value;
    private Metric metric = this;

    @ConfigArgSet(required = true, desc = "The value at which percentile will be calculated.")
    public Percentile setValue(String value) {
        this.value = Double.valueOf(value.substring(0,value.length() - 1));
        if (this.name.equals(getClass().getSimpleName())) {
            this.name = value;
        }
        return this;
    }

    @Override
    public ResultInfo<Long> getResult(final RunMap runMap, final AbstractTest testInstance) {
        return new ResultInfo<Long>() {
            @Override
            public String getFormat() {
                return "%d";
            }

            @Override
            public String getUnitOfMeasure() {
                return "ms";
            }

            @Override
            public Long getValue() {
                return runMap.getRecord(testInstance).getValueAtPercentile(value);
            }

            @Override
            public String getName() {
                return metric.getName();
            }

            @Override
            public int getDecimals() {
                return metric.getDecimals();
            }
        };
    }
}
