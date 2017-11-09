package com.adobe.qe.toughday.api.core.benchmark.signatures;

@FunctionalInterface
public interface VoidCallable {
    void call() throws Throwable;
}