package com.adobe.qe.toughday.runners;

import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.core.ChildTestFailedException;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.SkippedTestException;
import com.adobe.qe.toughday.tests.async.AsyncTestBase;

import java.lang.Throwable;
/**
 *
 */
public class AsyncTestRunner extends AbstractTestRunner<AsyncTestBase> {
    public AsyncTestRunner(Class<AsyncTestBase> testClass) {
        super(testClass);
    }

    @Override
    protected void run(AsyncTestBase testObject, RunMap runMap) throws Throwable {

    }
}
