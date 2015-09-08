package com.day.qa.toughday.tests.serial_tests;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.core.AbstractTest;

/**
 * Created by tuicu on 08/09/15.
 */
public class DummyGet extends TestBase {

    @Override
    public void test() throws ClientException {
        getDefaultClient().http().doGet("/");
    }

    @Override
    public AbstractTest newInstance() {
        return new DummyGet();
    }
}
