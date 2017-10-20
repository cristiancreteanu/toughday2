package com.adobe.qe.toughday.runners;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;

/**
 * Runner for a sequential test.
 */
public class SequentialTestRunner extends AbstractTestRunner<SequentialTestBase> {
    public SequentialTestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(SequentialTestBase testObject, RunMap runMap) throws ToughDayException {
        Long start = System.nanoTime();
        try {
            testObject.test();
            Long elapsed = (System.nanoTime() - start) / 1000000l;
            runMap.recordRun(testObject, elapsed);
        }
        catch (SkippedTestException e) {
            runMap.recordSkipped(testObject, e);
            if (testObject.getParent() != null) {
                throw e;
            }
        }
        catch (Throwable e) {
            runMap.recordFail(testObject, e);
            if(testObject.getParent() != null) { //don't let exceptions get to the suite
                throw new ChildTestFailedException(e);
            }
        }
    }
}
