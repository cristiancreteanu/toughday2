package com.day.qa.toughday.tests.async_tests;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.runners.AsyncTestRunner;
import org.apache.http.HttpRequest;

import java.util.List;

/**
 * Created by tuicu on 04/09/15.
 */
public abstract class AsyncTestBase extends AbstractTest {

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return AsyncTestRunner.class;
    }

    public abstract List<HttpRequest> test();
}
