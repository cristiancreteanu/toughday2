package com.adobe.qe.toughday.core.benckmark;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.labels.Nullable;

/**
 * Use this interface to create proxy factories. Useful when your proxy needs more logic than calling the default constructor
 * and the injections provided by the {@link Proxy} interface in order to be constructed.
 */
public interface ProxyFactory<T> {
    /**
     * Create a new proxy
     *
     * @param target
     * @param test
     * @param benchmark
     * @return A proxy object. If {@code null} os returned, the default proxy will be used.
     */
    @Nullable T createProxy(T target, AbstractTest test, Benchmark benchmark);
}
