package com.adobe.qe.toughday.tests.utils;

import com.adobe.qe.toughday.api.annotations.labels.Nullable;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.labels.NotThreadSafe;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.api.core.benchmark.Proxy;
import com.adobe.qe.toughday.api.core.benchmark.ResultInfo;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@NotThreadSafe(description = "Don't reuse a proxy for requests. Always create a new proxy for a new request.")
public class SlingClientProxy extends SlingClient implements Proxy<SlingClient> {

    public SlingClientProxy(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    private AbstractTest test;
    private SlingClient target;
    private Benchmark benchmark;

    @Override
    public void setTest(AbstractTest parent) {
        this.test = parent;
    }

    @Override
    public void setTarget(SlingClient target) {
        this.target = target;
    }

    @Override
    public void setBenchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    @Override
    public Benchmark benchmark() {
        return benchmark;
    }

    private boolean recordResult = true;
    private TestResult<SlingHttpData> testResult;

    private boolean shouldIRecord() {
        boolean tmp = recordResult;
        recordResult = false;
        return tmp;
    }

    public SlingHttpResponse doGet(String requestPath, List<NameValuePair> parameters, List<Header> headers, int... expectedStatus) throws ClientException {
        boolean recordResultHere = shouldIRecord();
        ResultInfo<SlingHttpResponse, SlingHttpData> result = benchmark().computeTestResult(test, (TestResult<SlingHttpData> testResult) -> {
            return super.doGet(requestPath, parameters, headers, expectedStatus);
        });

        TestResult<SlingHttpData> currentResult = result.getTestResult();
        @Nullable SlingHttpResponse response = result.getReturnValue();
        Throwable throwable = result.getThrowable();

        currentResult.withData(this.testResult.getData());
        if(response != null) {
            currentResult.getData().withBytes(response.getContent().length());
        }

        if(recordResultHere) {
            doRecord(currentResult);
        } else {
            this.testResult = currentResult;
        }

        doThrow(throwable);
        return response;
    }

    @Override
    public SlingHttpResponse doRequest(HttpUriRequest request, List<Header> headers, int... expectedStatus) throws ClientException {
        boolean recordResultHere = shouldIRecord();
        ResultInfo<SlingHttpResponse, SlingHttpData> result = benchmark().computeTestResult(test, (TestResult<SlingHttpData> testResult) -> {
            return super.doRequest(request, headers, expectedStatus);
        });

        TestResult<SlingHttpData> currentResult = result.getTestResult();
        @Nullable SlingHttpResponse response = result.getReturnValue();
        Throwable throwable = result.getThrowable();

        currentResult.withData(this.testResult.getData());
        if(response != null) {
            currentResult.getData().withBytes(response.getContent().length());
        }

        if(recordResultHere) {
            doRecord(testResult);
        } else {
            this.testResult = currentResult;
        }

        doThrow(throwable);
        return response;
    }

    @Override
    public SlingHttpResponse doRawRequest(String method, String uri, List<Header> headers, int... expectedStatus) throws ClientException {
        boolean recordResultHere = shouldIRecord();

        ResultInfo<SlingHttpResponse, SlingHttpData> result = benchmark().computeTestResult(test, (TestResult<SlingHttpData> testResult) -> {
            SlingHttpResponse response = target.doRawRequest(method, uri, headers, expectedStatus);
            SlingHttpData data = testResult.getData() != null ? testResult.getData() : new SlingHttpData();
            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            long bytes = contentLengthHeader != null ? Long.parseLong(contentLengthHeader.getValue()) : response.getEntity().getContentLength();
            data.withMethod(method)
                    .withUrl(uri)
                    .withResponseCode(response.getStatusLine().getStatusCode())
                    .withUser(target.getUser())
                    .withBytes(bytes);
            testResult.withData(data);
            return response;
        });

        TestResult<SlingHttpData> currentResult = result.getTestResult();
        @Nullable SlingHttpResponse response = result.getReturnValue();
        Throwable throwable = result.getThrowable();
        currentResult.getData().withLatency(currentResult.getDuration());

        if(recordResultHere) {
            doRecord(currentResult);
        } else {
            this.testResult = currentResult;
        }

        doThrow(throwable);
        return response;
    }

    @Override
    public SlingHttpResponse doStreamRequest(HttpUriRequest request, List<Header> headers, int... expectedStatus) throws ClientException {
        boolean recordResultHere = shouldIRecord();

        ResultInfo<SlingHttpResponse, SlingHttpData> result = benchmark().computeTestResult(test, (TestResult<SlingHttpData> testResult) -> {
            SlingHttpResponse response = target.doStreamRequest(request, headers, expectedStatus);
            SlingHttpData data = testResult.getData() != null ? testResult.getData() : new SlingHttpData();
            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            long bytes = contentLengthHeader != null ? Long.parseLong(contentLengthHeader.getValue()) : response.getEntity().getContentLength();
            data.withMethod(request.getMethod())
                    .withUrl(request.getURI().toString())
                    .withResponseCode(response.getStatusLine().getStatusCode())
                    .withQuery(getQueryString(request))
                    .withUser(target.getUser())
                    .withBytes(bytes);

            testResult.withData(data);
            return response;
        });

        TestResult<SlingHttpData> currentResult = result.getTestResult();
        @Nullable SlingHttpResponse response = result.getReturnValue();
        Throwable throwable = result.getThrowable();
        currentResult.getData().withLatency(currentResult.getDuration());

        if(recordResultHere) {
            doRecord(currentResult);
        } else {
            this.testResult = currentResult;
        }

        doThrow(throwable);
        return response;
    }

    private List<Map<String, String>> getQueryString(HttpUriRequest request) {
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase httpRequest = (HttpEntityEnclosingRequestBase) request;
            if(httpRequest.getEntity() instanceof UrlEncodedFormEntity) {
                UrlEncodedFormEntity urlEncodedFormEntity = (UrlEncodedFormEntity) httpRequest.getEntity();
                try {
                    List<Map<String, String>> query = new ArrayList<>();
                    for(NameValuePair nameValuePair : URLEncodedUtils.parse(urlEncodedFormEntity)) {
                        query.add(Collections.singletonMap(nameValuePair.getName(), nameValuePair.getValue()));
                    }
                    return query;
                } catch (Exception e) {
                }
            }
        }

        return null;
    }

    private void doRecord(TestResult<SlingHttpData> currentTestResult) {
        benchmark().getRunMap().record(currentTestResult);
        recordResult = true;
        this.testResult = null;
    }

    private void doThrow(Throwable throwable) throws ClientException{
        if(throwable != null) {
            if (throwable instanceof ClientException) {
                throw (ClientException) throwable;
            }
            throw new ClientException(throwable.getMessage(), throwable);
        }
    }
}
