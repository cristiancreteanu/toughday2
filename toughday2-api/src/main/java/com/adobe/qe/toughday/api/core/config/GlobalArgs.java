package com.adobe.qe.toughday.api.core.config;

import org.apache.logging.log4j.Level;

public interface GlobalArgs {
    long getDuration();
    long getTimeout();
    String getHost();
    int getPort();
    String getUser();
    String getPassword();
    String getProtocol();
    String getAuthMethod();
    boolean getInstallSampleContent();
    String getContextPath();
    Level getLogLevel();
    boolean getDryRun();
    boolean getSaveConfig();
    boolean getShowSteps();
    boolean getHostValidationEnabled();
}
