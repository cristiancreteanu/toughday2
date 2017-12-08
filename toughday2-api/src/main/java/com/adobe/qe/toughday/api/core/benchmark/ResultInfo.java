package com.adobe.qe.toughday.api.core.benchmark;

import com.adobe.qe.toughday.api.annotations.labels.Nullable;

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
    @Nullable R getReturnValue();

    /**
     * Any throwable that might have occurred
     */
    @Nullable Throwable getThrowable();
}
