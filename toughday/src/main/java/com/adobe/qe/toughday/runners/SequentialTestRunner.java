package com.adobe.qe.toughday.runners;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.SkippedTestException;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import com.adobe.qe.toughday.core.ChildTestFailedException;

/**
 * Runner for a sequential test.
 */
public class SequentialTestRunner extends AbstractTestRunner<SequentialTestBase> {
    public SequentialTestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(SequentialTestBase testObject, RunMap runMap) throws ChildTestFailedException, SkippedTestException {
        Long start = System.nanoTime();
        try {
            testObject.test();
            Long elapsed = (System.nanoTime() - start) / 1000000l;
            runMap.recordRun(testObject, elapsed);
        }
        catch (SkippedTestException e) {
            runMap.recordSkipped(testObject, e);
            if (testObject.getParent() != null) {
                throw new SkippedTestException(e);
            }
        }
        catch (Exception e) {
            runMap.recordFail(testObject, e);
            if(testObject.getParent() != null) { //don't let exceptions get to the suite
                throw new ChildTestFailedException(e);
            }
        }
    }
}
