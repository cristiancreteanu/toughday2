package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;

public class Skipped extends Metric {
    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<Long> resultInfo = new ResultInfo<Long>(this) {
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
                return runMap.getRecord(testInstance).getSkippedRuns();
            }
        };

        return resultInfo;
    }
}
