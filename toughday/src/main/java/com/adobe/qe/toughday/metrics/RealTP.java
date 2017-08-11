package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Number of runs divided by elapsed time. Formula: Runs / elapsed execution time.")
public class RealTP extends Metric {
    private Metric metric = this;

    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        ResultInfo<Double> resultInfo = new ResultInfo<Double>() {
            @Override
            public String getFormat() {
                return "%." + this.getDecimals() + "f";
            }

            @Override
            public String getUnitOfMeasure() {
                return "rps";
            }

            @Override
            public Double getValue() {
                return runMap.getRecord(testInstance).getRealThroughput();
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
