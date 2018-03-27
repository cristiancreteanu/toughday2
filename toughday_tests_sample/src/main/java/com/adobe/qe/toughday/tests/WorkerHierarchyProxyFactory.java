package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.api.core.benchmark.ProxyFactory;
import com.adobe.qe.toughday.api.core.benchmark.ResultInfo;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;

public class WorkerHierarchyProxyFactory implements ProxyFactory<Worker> {
    @Override
    public Worker createProxy(Worker target, AbstractTest test, Benchmark benchmark) {
        Worker proxy = Mockito.spy(target);
        try {
            Mockito.doAnswer(new Answer<Long>() {
                @Override
                public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                    long millis = invocationOnMock.getArgument(0);
                    ResultInfo<Long, Object> result = benchmark.computeTestResult(test, (TestResult<Object> testResult) -> {
                        return (Long) invocationOnMock.callRealMethod();
                    });

                    TestResult currentTestResult = result.getTestResult();
                    currentTestResult.withData(Collections.singletonMap("sleep", millis));
                    benchmark.getRunMap().record(currentTestResult);

                    if(result.getThrowable() != null) { throw result.getThrowable(); }
                    return result.getReturnValue();
                }
            }).when(proxy).doWork(Mockito.anyLong());
        } catch (Throwable throwable) {
            test.logger().warn("Could not create proxy", throwable);
            return target;
        }

        return proxy;
    }
}
