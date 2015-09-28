package com.day.qa.toughday.suitesetups;

import com.day.qa.toughday.core.SuiteSetup;

/**
 * Created by tuicu on 04/09/15.
 * Dummy suite setup. Part of the POC.
 */
public class DummySuiteSetup implements SuiteSetup {
    @Override
    public void setup() throws Exception {
        System.out.println("Suite setup completed :)");
    }
}
