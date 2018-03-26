package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.api.core.benchmark.Proxy;
import com.adobe.qe.toughday.api.core.benchmark.ResultInfo;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;

import java.util.Collections;

public class WorkerProxy extends Worker implements Proxy<Worker> {
    private final String info;
    private AbstractTest test;
    private Worker target;
    private Benchmark benchmark;
    
    public WorkerProxy(String info) {
        this.info = info;
    }

    @Override
    public long doWork(long millis) throws Throwable {
        ResultInfo<Long, Object> result = benchmark().computeTestResult(test, (TestResult<Object> testResult) -> {
            return super.doWork(millis);
        });

        TestResult currentTestResult = result.getTestResult();
        currentTestResult.withData(Collections.singletonMap("sleep", millis));
        benchmark().getRunMap().record(currentTestResult);

        if(result.getThrowable() != null) { throw result.getThrowable(); }
        return result.getReturnValue();
    }

    /* ----------------- Proxy Interface implementation --------------------------- */
    @Override
    public void setTest(AbstractTest test) { this.test = test; }

    @Override
    public void setTarget(Worker target) { this.target = target; }

    @Override
    public void setBenchmark(Benchmark benchmark) { this.benchmark = benchmark; }

    @Override
    public Benchmark benchmark() { return benchmark; }
    /* --------------------------------------------------------------------------- */
}
