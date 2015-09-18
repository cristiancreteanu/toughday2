package com.day.qa.toughday.core;

import com.day.qa.toughday.core.config.ConfigArg;

/**
 * Created by tuicu on 07/09/15.
 */
public class GlobalArgs {
    private String host;
    private int port;
    private String user;
    private String password;
    private static GlobalArgs instance;

    public static void setInstance(GlobalArgs instance) {
        if (GlobalArgs.instance == null)
            GlobalArgs.instance = instance;
    }

    public static GlobalArgs getInstance() {
        return instance;
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
