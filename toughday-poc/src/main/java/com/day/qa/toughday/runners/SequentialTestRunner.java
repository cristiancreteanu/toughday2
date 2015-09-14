package com.day.qa.toughday.runners;

import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.core.ChildTestFailedException;
import com.day.qa.toughday.core.RunMap;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.tests.sequential.SequentialTestBase;

/**
 * Created by tuicu on 04/09/15.
 */
public class SequentialTestRunner extends AbstractTestRunner<SequentialTestBase> {
    public SequentialTestRunner(Class<? extends AbstractTest> testClass) {
        super(testClass);
    }

    @Override
    protected void run(SequentialTestBase testObject, RunMap runMap) throws ChildTestFailedException {
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
            if(testObject.getParent() != null) { //don't let exceptions get to the suite
                throw new ChildTestFailedException(e);
            }
        }
    }
}
