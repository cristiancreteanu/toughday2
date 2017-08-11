package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Highest duration of test execution.")
public class Max extends Metric {
    Metric metric = this;

    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<Long> resultInfo = new ResultInfo<Long>() {
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
                return runMap.getRecord(testInstance).getMaxDuration();
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
