package com.adobe.qe.toughday.mocks;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;

import java.util.ArrayList;
import java.util.List;

public class MockTest extends AbstractTest {
    List<AbstractTest> noChildren = new ArrayList<>();

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
        return null;
    }
}
