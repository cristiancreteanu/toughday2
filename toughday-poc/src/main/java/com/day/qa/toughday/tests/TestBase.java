package com.day.qa.toughday.tests;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.AbstractTestRunner;
import com.day.qa.toughday.TestRunner;


/**
 * Created by tuicu on 04/09/15.
 */
public abstract class TestBase extends AbstractTest {

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return TestRunner.class;
    }

    public abstract void test() throws ClientException;
}
