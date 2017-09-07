package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Internal;

@Internal
@Description(desc = "Timestamp of the last finished test run.")
public class Timestamp extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getTimestamp();
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
