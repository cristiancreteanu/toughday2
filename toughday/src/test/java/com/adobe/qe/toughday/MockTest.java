package com.adobe.qe.toughday;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MockTest extends AbstractTest {
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
        return null;
    }
}
