package com.adobe.qe.toughday.core.benckmark;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.SkippedTestException;
import com.adobe.qe.toughday.core.annotations.labels.Nullable;
import com.adobe.qe.toughday.core.benckmark.signatures.Callable;
import com.adobe.qe.toughday.core.benckmark.signatures.InjectTestResultCallable;
import com.adobe.qe.toughday.core.benckmark.signatures.VoidCallable;
import com.adobe.qe.toughday.core.benckmark.signatures.VoidInjectTestResultCallable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Modifier;
import java.util.UUID;

/**
 * Implementation of the {@link Benchmark} interface. See the interface for documentation.
 */
public class BenchmarkImpl implements Benchmark {

    private ProxiesContainer proxiesContainer = new ProxiesContainer();
    private RunMap runMap;

    @Override
    public <T, F extends T> void registerClassProxy(Class<T> klass, Class<F> proxyClass) {
        proxiesContainer.registerClassProxy(klass, proxyClass);
    }

    @Override
    public <T> void registerClassProxyFactory(Class<T> klass, ProxyFactory<T> proxyFactory) {
        proxiesContainer.registerClassProxyFactory(klass, proxyFactory);
    }

    @Override
    public <T> void registerHierarchyProxyFactory(Class<T> klass, ProxyFactory<T> proxyFactory) {
        proxiesContainer.registerHierarchyFactory(klass, proxyFactory);
    }

    @Override
    public void setRunMap(RunMap runMap) {
        this.runMap = runMap;
    }

    @Override
    public RunMap getRunMap() {
        return runMap;
    }

    /*  ############ Default proxy flavour. Default proxy will use the simple lambda flavour. ############ */

    @Override
    public <R, K> Triple<TestResult<K>, R, Throwable> computeTestResult(AbstractTest test, InjectTestResultCallable<R, K> callable) {
        Throwable throwableResult = null;
        R callableResult = null;
        TestResult<K> testResult = new TestResult<K>(test)
                .beginBenchmark();
        try {
            callableResult = callable.call(testResult);
        }
        catch (SkippedTestException e) {
            testResult.markAsSkipped(e);
            throwableResult = e;
        }
        catch (Throwable e) {
            testResult.markAsFailed(e);
            throwableResult = e;
        }
        if(Thread.interrupted() && throwableResult == null) {
            throwableResult = new InterruptedException("Timeout occurred.");
            testResult.markAsFailed(throwableResult);
        }
        testResult.endBenchmark();
        if(test instanceof AdHocTest) {
            testResult.withShowInAggregatedView(test.getShowStepsResolved());
        }

        return new ImmutableTriple(testResult, callableResult, throwableResult);
    }

    @Override
    public <T> T measure(AbstractTest test, T object) throws Throwable {
        T proxyObject = proxiesContainer.getProxy(object, test, this);
        if (proxyObject == null) {
            proxyObject = createDefaultProxy(test, object);
        }
        return measure(test, object, proxyObject);
    }

    public <T> T measure(@Nullable UUID id, AbstractTest parent, String label, T object) throws Throwable {
        return measure(new AdHocTest(id, parent, label), object);
    }

    @Override
    public <T> T measure(AbstractTest parent, String label, T object) throws Throwable {
        return measure((UUID) null, parent, label, object);
    }

    /*  ############ User defined proxy flavour ############ */
    @Override
    public <T> T measure(AbstractTest test, T object, T proxy) throws Throwable {
        if(proxy instanceof Proxy) {
            Proxy<T> p = (Proxy<T>) proxy;
            p.setTest(test);
            p.setTarget(object);
            p.setBenchmark(this);
        }
        return proxy;
    }

    public <T> T measure(@Nullable UUID id, AbstractTest parent, String label, T object, T proxy) throws Throwable {
        return measure(new AdHocTest(id, parent, label), object, proxy);
    }

    @Override
    public <T> T measure(AbstractTest parent, String label, T object, T proxy) throws Throwable {
        return measure(null, parent, label, object, proxy);
    }

    /*  ############ Simple lambda flavour ############ */
    @Override
    public <T> T measure(AbstractTest test, Callable<T> callable) throws Throwable {
        Triple<TestResult<Object>, T, Throwable> result = computeTestResult(test, (TestResult<Object> testResult) -> {
            return callable.call();
        });
        runMap.record(result.getLeft());
        Throwable throwable = result.getRight();
        if(throwable != null) throw throwable;
        return result.getMiddle();
    }

    public <T> T measure(@Nullable UUID id, AbstractTest parent, String label, Callable<T> callable) throws Throwable {
        return measure(new AdHocTest(id, parent, label), callable);
    }

    @Override
    public <T> T measure(AbstractTest parent, String label, Callable<T> callable) throws Throwable {
        return measure(null, parent, label, callable);
    }

    /*  ############ Simple void lambda flavour ############ */
    @Override
    public void measure(AbstractTest test, VoidCallable callable) throws Throwable {
        Triple<TestResult<Object>, Void, Throwable> result = computeTestResult(test, (TestResult<Object> testResult) -> {
            callable.call();
            return null;
        });
        runMap.record(result.getLeft());
        Throwable throwable = result.getRight();
        if(throwable != null) throw throwable;
    }

    public void measure(@Nullable UUID id, AbstractTest parent, String label, VoidCallable callable) throws Throwable {
        measure(new AdHocTest(id, parent, label), callable);
    }

    @Override
    public void measure(AbstractTest parent, String label, VoidCallable callable) throws Throwable {
        measure((UUID) null, parent, label, callable);
    }

    @Override
    public <T, K> T measure(AbstractTest test, InjectTestResultCallable<T, K> callable) throws Throwable {
        Triple<TestResult<K>, T, Throwable> result = this.computeTestResult(test, (TestResult<K> testResult) -> {
            return callable.call(testResult);
        });

        runMap.record(result.getLeft());
        Throwable throwable = result.getRight();
        if(throwable != null) throw throwable;
        return result.getMiddle();
    }

    public <T, K> T measure(@Nullable UUID id, AbstractTest parent, String label, InjectTestResultCallable<T, K> callable) throws Throwable {
        return measure(new AdHocTest(id, parent, label), callable);
    }

    @Override
    public <T, K> T measure(AbstractTest parent, String label, InjectTestResultCallable<T, K> callable) throws Throwable {
        return measure((UUID) null, parent, label, callable);
    }

    /* ############ Injected void lambda flavour ############ */
    @Override
    public <K> void measure(AbstractTest test, VoidInjectTestResultCallable<K> callable) throws Throwable {
        Triple<TestResult<K>, Void, Throwable> result = this.computeTestResult(test, (TestResult<K> testResult) -> {
            callable.call(testResult);
            return null;
        });
        runMap.record(result.getLeft());
        Throwable throwable = result.getRight();
        if(throwable != null) throw throwable;
    }

    public <K> void measure(@Nullable UUID id, AbstractTest parent, String label, VoidInjectTestResultCallable<K> callable) throws Throwable {
        measure(new AdHocTest(id, parent, label), callable);
    }

    @Override
    public <K> void measure(AbstractTest parent, String label, VoidInjectTestResultCallable<K> callable) throws Throwable {
        measure((UUID) null, parent, label, callable);
    }

    private <T> T createDefaultProxy(AbstractTest test, T object) throws Throwable {
        return Mockito.mock((Class<T>)object.getClass(), new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return measure(test, new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if(!Modifier.isPublic(invocation.getMethod().getModifiers())) {
                            invocation.getMethod().setAccessible(true);
                        }
                        return invocation.getMethod().invoke(object, ProxyHelpers.canonicArguments(invocation.getMethod(), invocation.getArguments()));
                    }
                });
            }
        });
    }

    public BenchmarkImpl clone() {
        BenchmarkImpl clone = new BenchmarkImpl();
        clone.proxiesContainer = this.proxiesContainer.clone();
        return clone;
    }
}
