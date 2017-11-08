package com.adobe.qe.toughday.core.benckmark;

import com.adobe.qe.toughday.core.AbstractTest;

/**
 * If your custom proxy implements this interface, the setters from it will be called to inject information into
 * your proxy.
 * @param <T> Type of the object being proxied
 */
public interface Proxy<T> {
    /**
     * Method for injecting the test
     * @param test
     */
    void setTest(AbstractTest test);

    /**
     * Method for injecting the target
     * @param target
     */
    void setTarget(T target);

    /**
     * Method for injecting the benchmark object
     * @param benchmark
     */
    void setBenchmark(Benchmark benchmark);

    /**
     * Method for getting the injected benchmark object
     * @return
     */
    Benchmark benchmark();
}
