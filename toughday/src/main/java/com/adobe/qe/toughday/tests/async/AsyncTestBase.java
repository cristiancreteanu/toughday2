package com.adobe.qe.toughday.tests.async;

import com.adobe.qe.toughday.runners.AsyncTestRunner;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import org.apache.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for async tests.
 */
public abstract class AsyncTestBase extends AbstractTest {
    private static final List<AbstractTest> noChildren = new ArrayList<>();

    @Override
    public List<AbstractTest> getChildren() {
        return noChildren;
    }


    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return AsyncTestRunner.class;
    }

    public abstract List<HttpRequest> test();
}
