package com.adobe.qe.toughday.tests.utils;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.api.core.benchmark.ProxyFactory;

import com.adobe.qe.toughday.api.core.benchmark.ProxyHelpers;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Mockito.*;

public class SlingClientsProxyFactory implements ProxyFactory<SlingClient> {

    @Override
    public SlingClient createProxy(SlingClient target, AbstractTest test, Benchmark benchmark) {
        try {
            SlingClient slingClientProxy = spy(target);
            SlingClientProxy proxy = target.adaptTo(SlingClientProxy.class);
            proxy.setTest(test);
            proxy.setTarget(target);
            proxy.setBenchmark(benchmark);

            doAnswer(doStreamRequestAnswer(proxy)).when(slingClientProxy).doStreamRequest(any(), any(), any());
            doAnswer(doRawRequestAnswer(proxy)).when(slingClientProxy).doRawRequest(any(), any(), any(), any());
            doAnswer(doRequestAnswer(proxy)).when(slingClientProxy).doRequest(any(), any(), any());
            doAnswer(doGetAnswer(proxy)).when(slingClientProxy).doGet(any(), any(), any(), any());

            return slingClientProxy;
        } catch (ClientException e) {
            //If something goes wrong and we can't create a proxy, return the original object
            return target;
        }
    }

    private Answer<SlingHttpResponse> doStreamRequestAnswer(SlingClientProxy proxy) {
        return invocation -> {
            Object[] arguments = ProxyHelpers.canonicArguments(invocation.getMethod(), invocation.getArguments());
            return proxy.doStreamRequest((HttpUriRequest) arguments[0], (List<Header>) arguments[1], (int[])arguments[2]);
        };
    }

    private Answer<SlingHttpResponse> doRawRequestAnswer(SlingClientProxy proxy) {
        return invocation -> {
            Object[] arguments = ProxyHelpers.canonicArguments(invocation.getMethod(), invocation.getArguments());
            return proxy.doRawRequest((String) arguments[0], (String) arguments[1], (List<Header>) arguments[2], (int[]) arguments[3]);
        };
    }

    private Answer<SlingHttpResponse> doRequestAnswer(SlingClientProxy proxy) {
        return invocation -> {
            Object[] arguments = ProxyHelpers.canonicArguments(invocation.getMethod(), invocation.getArguments());
            return proxy.doRequest((HttpUriRequest) arguments[0], (List<Header>) arguments[1], (int[]) arguments[2]);
        };
    }

    private Answer<SlingHttpResponse> doGetAnswer(SlingClient proxy) {
        return invocation -> {
            Object[] arguments = ProxyHelpers.canonicArguments(invocation.getMethod(), invocation.getArguments());
            return proxy.doGet((String)arguments[0], (List<NameValuePair>)arguments[1], (List<Header>)arguments[2], (int[]) arguments[3]);
        };
    }
}
