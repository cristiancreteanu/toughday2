package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;
import com.adobe.qe.toughday.api.core.RunMap;


public class MyTestRunner extends AbstractTestRunner<MyTestBase> {

    public MyTestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(MyTestBase testObject, RunMap runMap) throws Throwable {
        for(int i = 0; i < 10; i++) {
            testObject.benchmark().measure(testObject, () -> {
                testObject.myTest();
            });
        }
    }
}
