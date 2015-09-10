package com.day.qa.toughday.runners;

import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.core.ChildTestFailedException;
import com.day.qa.toughday.core.RunMap;
import com.day.qa.toughday.tests.async_tests.AsyncTestBase;

/**
 * Created by tuicu on 04/09/15.
 */
public class AsyncTestRunner extends AbstractTestRunner<AsyncTestBase> {
    public AsyncTestRunner(Class<AsyncTestBase> testClass) {
        super(testClass);
    }

    @Override
    protected void run(AsyncTestBase testObject, RunMap runMap) throws ChildTestFailedException {

    }
}
