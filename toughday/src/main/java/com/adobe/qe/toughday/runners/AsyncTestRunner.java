package com.adobe.qe.toughday.runners;

import com.adobe.qe.toughday.core.*;
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
    protected void run(AsyncTestBase testObject, RunMap runMap) throws ToughDayException {

    }
}
