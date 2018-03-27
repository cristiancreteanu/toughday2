package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.tests.sequential.AEMTestBase;

public class AEMHello extends AEMTestBase {
    @Override
    public void test() throws Throwable {
        benchmark().measure(this, "CRXDE", getDefaultClient()).doGet("/crx/de");
    }

    @Override
    public AbstractTest newInstance() {
        return new AEMHello();
    }
}
