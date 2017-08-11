package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Number of successful runs.")
public class Passed extends Metric {
    Metric metric = this;

    @Override
    public ResultInfo<Long> getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<Long> resultInfo = new ResultInfo<Long>() {
            @Override
            public String getFormat() {
                return "%d";
            }

            @Override
            public String getUnitOfMeasure() {
                return "";
            }

            @Override
            public Long getValue() {
                return runMap.getRecord(testInstance).getTotalRuns();
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
        return resultInfo;
    }

}
