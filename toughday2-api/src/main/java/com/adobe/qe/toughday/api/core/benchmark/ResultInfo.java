package com.adobe.qe.toughday.api.core.benchmark;

/**
 * A wrapper over the outcome of a test run.
 */
public interface ResultInfo<R, K> {
    /**
     * The {@link TestResult} generated
     */
    TestResult<K> getTestResult();

    /**
     * (Optional) The return value
     */
    R getReturnValue();

    /**
     * Any throwable that might have occurred
     */
    Throwable getThrowable();
}
