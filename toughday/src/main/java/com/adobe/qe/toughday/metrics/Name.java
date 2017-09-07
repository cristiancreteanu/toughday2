package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Internal;

@Internal
@Description(desc = "The name of the test.")
public class Name extends Metric {

    @Override
    public Object getValue(RunMap.TestEntry testEntry) {
        return testEntry.getTest().getFullName();
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
