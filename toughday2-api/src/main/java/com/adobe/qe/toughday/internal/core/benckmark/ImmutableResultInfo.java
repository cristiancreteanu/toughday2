package com.adobe.qe.toughday.internal.core.benckmark;

import com.adobe.qe.toughday.api.core.benchmark.ResultInfo;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;

public class ImmutableResultInfo<R, K> implements ResultInfo<R, K> {
    private final TestResult<K> testResult;
    private final R returnValue;
    private final Throwable throwable;

    public ImmutableResultInfo(TestResult<K> testResult, R returnValue, Throwable throwable) {
        this.testResult = testResult;
        this.returnValue = returnValue;
        this.throwable = throwable;
    }

    @Override
    public TestResult<K> getTestResult() {
        return testResult;
    }

    @Override
    public R getReturnValue() {
        return returnValue;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }
}
