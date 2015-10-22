package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.qe.toughday.core.AbstractTest;

/**
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
