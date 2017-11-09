package com.adobe.qe.toughday.api.core;

import com.adobe.qe.toughday.runners.SequentialTestRunner;

import java.util.ArrayList;
import java.util.List;

public abstract class SequentialTest extends AbstractTest  {
    private static final List<AbstractTest> noChildren = new ArrayList<>();

    @Override
    public List<AbstractTest> getChildren() {
        return noChildren;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return SequentialTestRunner.class;
    }

    public abstract void test() throws Throwable;
}
