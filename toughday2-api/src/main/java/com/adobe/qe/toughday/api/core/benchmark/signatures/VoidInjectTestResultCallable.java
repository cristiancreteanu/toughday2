package com.adobe.qe.toughday.api.core.benchmark.signatures;

import com.adobe.qe.toughday.api.core.benchmark.TestResult;

@FunctionalInterface
public interface VoidInjectTestResultCallable<K> {
    void call(TestResult<K> testResult) throws Throwable;
}
