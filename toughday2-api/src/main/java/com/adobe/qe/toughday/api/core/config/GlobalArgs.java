package com.adobe.qe.toughday.api.core.config;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import org.apache.logging.log4j.Level;

/**
 * Created by tuicu on 10/11/17.
 */
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
}
