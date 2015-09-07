package com.day.qa.toughday.suite_setups;

import com.day.qa.toughday.core.SuiteSetup;

/**
 * Created by tuicu on 04/09/15.
 */
public class DummySuiteSetup implements SuiteSetup {
    @Override
    public void setup() throws Exception {
        System.out.println("Suite setup completed :)");
    }
}
