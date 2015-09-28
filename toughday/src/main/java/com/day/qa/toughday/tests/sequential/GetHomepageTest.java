package com.day.qa.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.core.AbstractTest;

/**
 * Created by tuicu on 08/09/15.
 * Test for getting the homepage.
 */
public class GetHomepageTest extends SequentialTestBase {

    @Override
    public void test() throws ClientException {
        getDefaultClient().http().doGet("/");
    }

    @Override
    public AbstractTest newInstance() {
        return new GetHomepageTest();
    }
}
