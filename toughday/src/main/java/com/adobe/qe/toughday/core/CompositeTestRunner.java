package com.adobe.qe.toughday.core;

/**
 * Runner for CompositeTest.
 */
public class CompositeTestRunner extends  AbstractTestRunner<CompositeTest> {

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
     * @throws ChildTestFailedException if its part of another composite test and a step fails.
     */
    @Override
    protected void run(CompositeTest testObject, RunMap runMap) throws ToughDayException {
        Long start = System.nanoTime();
        for(AbstractTest child : testObject.getChildren()) {
            AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(child);
            try {
                runner.runTest(child, runMap);
            } catch (SkippedTestException e) {
                runMap.recordSkipped(testObject, e);
                if (testObject.getParent() != null) {
                    throw e;
                } else {
                    return; //don't let exceptions get to the suite
                }
            }
            catch (ToughDayException e) {
                runMap.recordFail(testObject, e);
                if(testObject.getParent() != null) {
                    throw e;
                } else {
                    return; //don't let exceptions get to the suite
                }
            }
            catch (Throwable e) {
                runMap.recordFail(testObject, e);
                if(testObject.getParent() != null) {
                    throw new ToughDayException(e);
                } else {
                    return; //don't let exceptions get to the suite
                }
            }
            /* Timeout can occur between steps of the composite test */
            if(Thread.interrupted()) {
                ChildTestFailedException timeout = new ChildTestFailedException(new InterruptedException());
                runMap.recordFail(testObject, timeout);
                if(testObject.getParent() != null) {
                    throw timeout;
                } else {
                    return; //don't let exceptions get to the suite
                }
            }
        }
        Long elapsed = (System.nanoTime() - start) / 1000000l;
        synchronized (runMap) {
            runMap.recordRun(testObject, elapsed);
        }
    }
}
