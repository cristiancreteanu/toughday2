package com.adobe.qe.toughday;

import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;

import java.util.ArrayList;
import java.util.List;

public class MockTestRequiredField extends AbstractTest {
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
        return null;
    }

    @Override
    @ConfigArgSet(required = true)
    public AbstractTest setName(String name) {
        return super.setName(name);
    }
}
