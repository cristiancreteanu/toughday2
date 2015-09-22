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
    private long waitTime;
    private long duration;
    private List<Publisher> publishers;
    private long timeout;

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

    private static long unitToSeconds(char unit) {
        long factor = 1;
        switch (unit) {
            case 'd': factor *= 24;
            case 'h': factor *= 60;
            case 'm': factor *= 60;
            case 's': factor *= 1;
                break;
            default:
                throw new IllegalArgumentException("Unknown duration unit: " + unit);
        }
        return factor;
    }

    private static long parseDurationToSeconds(String duration) {
        long finalDuration = 0l;
        long intermDuration = 0;

        for(char c : duration.toCharArray()) {
            if(Character.isDigit(c)) {
                intermDuration = intermDuration * 10 + (long) (c - '0');
            } else {
                finalDuration += intermDuration * unitToSeconds(c);
                intermDuration = 0;
            }
        }
        return finalDuration;
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
        this.duration = parseDurationToSeconds(durationString);
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

    public long getWaitTime() {
        return waitTime;
    }

    public long getDuration() {
        return duration;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public long getTimeout() {
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
