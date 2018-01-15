package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;
import com.adobe.qe.toughday.api.annotations.Internal;

@Internal
@Description(desc = "The name of the test.")
public class Name extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getTest().getFullName();
    }

    @Override
    public String getFormat() {
        return "%s";
    }

    @Override
    public String getUnitOfMeasure() {
        return "";
    }

}
