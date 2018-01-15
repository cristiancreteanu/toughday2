package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.RunMap;
import com.adobe.qe.toughday.api.annotations.Internal;

@Internal
@Description(desc = "Timestamp of the last finished test run.")
public class Timestamp extends Metric {

    @Override
    public Object getValue(RunMap.TestStatistics testStatistics) {
        return testStatistics.getTimestamp();
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
