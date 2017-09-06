package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;

@Description(desc = "Standard deviation.")
public class StdDev extends Metric {
    @Override
    public ResultInfo getResult(final RunMap runMap, final AbstractTest testInstance) {
        ResultInfo<Double> resultInfo = new ResultInfo<Double>(this) {
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
                return runMap.getRecord(testInstance).getStandardDeviation();
            }

        };
        return resultInfo;
    }
}
