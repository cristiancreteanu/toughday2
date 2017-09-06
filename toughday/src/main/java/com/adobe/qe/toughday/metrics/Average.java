package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Computed average duration of all test executions. Formula: Sum (request time) / Runs  .")
public class Average extends Metric {
    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<Double> resultInfo = new ResultInfo<Double>(this) {
            @Override
            public String getFormat() {
                return "%." + this.getDecimals() + "f";
            }

            @Override
            public String getUnitOfMeasure() {
                return "ms";
            }

            @Override
            public Double getValue() {
                return runMap.getRecord(testInstance).getAverageDuration();
            }
        };

        return resultInfo;
    }
}
