package com.day.qa.toughday.runners;

import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.core.RunMap;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.tests.TestBase;

/**
 * Created by tuicu on 04/09/15.
 */
public class TestRunner extends AbstractTestRunner<TestBase> {
    public TestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(TestBase testObject, RunMap runMap) {
        Long start = System.nanoTime();
        try {
            testObject.test();
            Long elapsed = (System.nanoTime() - start) / 1000000l;
            synchronized (runMap) {
                runMap.recordRun(testObject, elapsed);
            }
        }
        catch (Exception e) {
            synchronized (runMap) {
                runMap.recordFail(testObject, e);
            }
        }

    }
}
