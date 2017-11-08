package com.adobe.qe.toughday.core.benckmark.signatures;

import com.adobe.qe.toughday.core.benckmark.TestResult;

@FunctionalInterface
public interface VoidInjectTestResultCallable<K> {
    void call(TestResult<K> testResult) throws Throwable;
}
