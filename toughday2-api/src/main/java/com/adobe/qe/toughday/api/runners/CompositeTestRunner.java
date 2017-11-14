package com.adobe.qe.toughday.api.runners;

import com.adobe.qe.toughday.api.core.*;
import com.adobe.qe.toughday.api.core.RunMap;
import com.adobe.qe.toughday.api.core.SkippedTestException;
import com.adobe.qe.toughday.api.core.ChildTestFailedException;

/**
 * Runner for CompositeTest.
 */
public class CompositeTestRunner extends AbstractTestRunner<CompositeTest> {

    /**
     * Constructor
     * @param testClass
     */
    public CompositeTestRunner(Class testClass) {
        super(testClass);
    }

    /**
     * Method for running the test.
     * @param testObject instance of the test to run
     * @param runMap the run map in which the benchmark will be recorded.
     * @throws Throwable any throwable occurred in the test and was propagated upstream by the implementation runner
     */
    @Override
    protected void run(CompositeTest testObject, RunMap runMap) throws Throwable {
        testObject.benchmark().measure(testObject, () -> {
            for (AbstractTest child : testObject.getChildren()) {
                AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(child);
                runner.runTest(child, runMap);
            }
        });
    }
}
