package com.adobe.qe.toughday;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MockTest extends AbstractTest {
    private List<AbstractTest> noChildren = new ArrayList<>();

    @Override
    public List<AbstractTest> getChildren() {
        return noChildren;

    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return null;
    }

    @Override
    public AbstractTest newInstance() {
        return new MockTest();
    }

    @Override
    public Logger logger() {
        return null;
    }
}
