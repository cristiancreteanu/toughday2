package com.adobe.qe.toughday.core.benckmark.signatures;

@FunctionalInterface
public interface Callable<T> {
    T call() throws Throwable;
}