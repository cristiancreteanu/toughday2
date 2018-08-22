package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.api.core.config.GlobalArgs;
import org.apache.logging.log4j.Level;

public class MockGlobalArgs implements GlobalArgs {
    private long timeout = 180;

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getAuthMethod() {
        return null;
    }

    @Override
    public boolean getInstallSampleContent() {
        return false;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public Level getLogLevel() {
        return null;
    }

    @Override
    public boolean getDryRun() {
        return false;
    }

    @Override
    public boolean getSaveConfig() {
        return false;
    }

    @Override
    public boolean getShowSteps() {
        return false;
    }

    @Override
    public boolean getHostValidationEnabled() {
        return false;
    }
}
