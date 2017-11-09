package com.adobe.qe.toughday.api.core.benchmark.signatures;

import com.adobe.qe.toughday.api.core.benchmark.TestResult;

@FunctionalInterface
public interface InjectTestResultCallable<T, K> {
    T call(TestResult<K> testResult) throws Throwable;
}