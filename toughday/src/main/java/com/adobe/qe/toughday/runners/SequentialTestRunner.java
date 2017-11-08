package com.adobe.qe.toughday.runners;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.benckmark.TestResult;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;

/**
 * Runner for a sequential test.
 */
public class SequentialTestRunner extends AbstractTestRunner<SequentialTestBase> {
    public SequentialTestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(SequentialTestBase testObject, RunMap runMap) throws Throwable {
            testObject.benchmark().measure(testObject, ()->{
                testObject.test();
            });
    }
}
