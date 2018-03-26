package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.api.core.benchmark.ProxyFactory;

public class WorkerProxyFactory implements ProxyFactory<Worker> {
    @Override
    public Worker createProxy(Worker target, AbstractTest test, Benchmark benchmark) {
        return new WorkerProxy("Such info!");
    }
}
