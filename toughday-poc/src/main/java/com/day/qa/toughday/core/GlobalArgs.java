package com.day.qa.toughday.core;

import com.day.qa.toughday.core.cli.CliArg;

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

    public GlobalArgs getInstance() {
        return instance;
    }

    @CliArg
    public void setHost(String host) {
        this.host = host;
    }

    @CliArg
    void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    @CliArg
    public void setUser(String user) {
        this.user = user;
    }

    @CliArg
    public void setPassword(String password) {
        this.password = password;
    }
}
