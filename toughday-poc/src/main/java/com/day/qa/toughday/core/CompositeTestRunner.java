package com.day.qa.toughday.core;

/**
 * Created by tuicu on 09/09/15.
 */
public class CompositeTestRunner extends  AbstractTestRunner<CompositeTest> {

    public CompositeTestRunner(Class testClass) {
        super(testClass);
    }

    @Override
    protected void run(CompositeTest testObject, RunMap runMap) throws ChildTestFailedException {
        Long start = System.nanoTime();
        for(AbstractTest child : testObject.getChildren()) {
            AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(child);
            try {
                runner.runTest(child, runMap);
            } catch (ChildTestFailedException e) {
                runMap.recordFail(testObject, e);
                if(testObject.getParent() != null) {
                    throw e;
                } else {
                    return;
                }
            }
        }
    }
}
