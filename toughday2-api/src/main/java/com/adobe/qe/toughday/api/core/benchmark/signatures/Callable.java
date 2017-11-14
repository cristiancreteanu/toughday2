package com.adobe.qe.toughday.api.core.benchmark.signatures;

@FunctionalInterface
public interface Callable<T> {
    T call() throws Throwable;
}