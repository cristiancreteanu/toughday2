package com.adobe.qe.toughday.mocks;

import com.adobe.qe.toughday.api.annotations.Internal;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.List;

@Internal
public class MockTest extends AbstractTest {
    private static Logger logger;

    @Override
    public List<AbstractTest> getChildren() {
        return null;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return null;
    }

    @Override
    public AbstractTest newInstance() {
        return null;
    }

    @Override
    public Logger logger() {
        return getLogger();
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new SimpleLogger("null", Level.INFO, false, false,
                    false, false, null,
                    null, new PropertiesUtil("."), null);
        }

        return logger;
    }
}
