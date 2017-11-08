package com.adobe.qe.toughday.tests.utils;

import java.util.List;
import java.util.Map;

public class SlingHttpData {
    private String url;
    private String method;
    private int responseCode;
    private List<Map<String, String>> query;
    private long bytes;
    private double latency = Double.NaN;
    private String user;

    public SlingHttpData() {
    }

    public <T extends SlingHttpData> T withUrl(String url) {
        this.url = url;
        return (T) this;
    }

    public <T extends SlingHttpData> T withMethod(String method) {
        this.method = method;
        return (T) this;
    }

    public <T extends SlingHttpData> T withResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return (T) this;
    }

    public <T extends SlingHttpData> T withQuery(List<Map<String, String>> query) {
        this.query = query;
        return (T) this;
    }

    public <T extends SlingHttpData> T withBytes(long bytes) {
        this.bytes = bytes;
        return (T) this;
    }

    public <T extends SlingHttpData> T withLatency(double latency) {
        this.latency = latency;
        return (T) this;
    }

    public <T extends SlingHttpData> T withUser(String user) {
        this.user = user;
        return (T) this;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public List<Map<String, String>> getQuery() {
        return query;
    }

    public long getBytes() {
        return bytes;
    }

    public String user() {
        return user;
    }

}
