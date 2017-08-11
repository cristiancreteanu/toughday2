package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Internal;

@Internal
@Description(desc = "The name of the test.")
public class Name extends Metric {
    Metric metric = this;

    @Override
    public ResultInfo<String> getResult(final RunMap runMap, final AbstractTest testInstance) {
        final ResultInfo<String> resultInfo = new ResultInfo<String>() {
            @Override
            public String getFormat() {
                return "%s";
            }

            @Override
            public String getUnitOfMeasure() {
                return "";
            }

            @Override
            public String getValue() {
                return runMap.getRecord(testInstance).getTest().getFullName();
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

        return  resultInfo;
    }
}
