package com.adobe.qe.toughday.mocks;

import com.adobe.qe.toughday.api.annotations.Internal;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;

import java.util.List;

@Internal
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
}
