package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.AbstractTestRunner;

import java.util.ArrayList;
import java.util.List;

public abstract class MyTestBase extends AbstractTest {
    private static final List<AbstractTest> EMPTY_LIST = new ArrayList<>();

    @Override
    public List<AbstractTest> getChildren() {
        return EMPTY_LIST;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return MyTestRunner.class;
    }

    @Override
    public abstract AbstractTest newInstance();

    public abstract void myTest() throws Throwable;
}
