package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Computed median duration of all test executions.")
public class Median extends Metric{
    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<Long> resultInfo = new ResultInfo<Long>(this) {
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
                return runMap.getRecord(testInstance).getMedianDuration();
            }

        };

        return resultInfo;
    }
}
