package com.day.qa.toughday.core;

import com.day.qa.toughday.core.config.ConfigArg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuicu on 07/09/15.
 */
public class GlobalArgs {
    private String host;
    private int port;
    private String user;
    private String password;
    private int concurrency;
    private int waitTime;
    private int duration;
    private List<Publisher> publishers;
    private int timeout;

    private static GlobalArgs instance;

    public static void setInstance(GlobalArgs instance) {
        if (GlobalArgs.instance == null)
            GlobalArgs.instance = instance;
    }

    public static GlobalArgs getInstance() {
        return instance;
    }

    public GlobalArgs() {
        publishers = new ArrayList<>();
    }

    @ConfigArg
    public void setHost(String host) {
        this.host = host;
    }

    @ConfigArg
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    @ConfigArg
    public void setUser(String user) {
        this.user = user;
    }

    @ConfigArg
    public void setPassword(String password) {
        this.password = password;
    }

    @ConfigArg
    public void setConcurrency(String concurrencyString) {
        this.concurrency = Integer.parseInt(concurrencyString);
    }

    @ConfigArg
    public void setDuration(String durationString) {
        this.duration = Integer.parseInt(durationString);
    }

    @ConfigArg
    public void setWaitTime(String waitTime) {
        this.waitTime = Integer.parseInt(waitTime);
    }

    @ConfigArg
    public void setTimeout(String timeout) {
        this.timeout = Integer.parseInt(timeout) * 1000;
    }

    public void addPublisher(Publisher publisher) {
        publishers.add(publisher);
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getDuration() {
        return duration;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
