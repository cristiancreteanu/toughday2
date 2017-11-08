package com.adobe.qe.toughday.core.benckmark.signatures;

import com.adobe.qe.toughday.core.benckmark.TestResult;

@FunctionalInterface
public interface InjectTestResultCallable<T, K> {
    T call(TestResult<K> testResult) throws Throwable;
}