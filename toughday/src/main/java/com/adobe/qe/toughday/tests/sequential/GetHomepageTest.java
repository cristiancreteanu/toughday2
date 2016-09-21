package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;

/**
 * Test for getting the homepage.
 */
public class GetHomepageTest extends SequentialTestBase {

    @Override
    public void test() throws Exception {
        getDefaultClient().doGet("/");
    }

    @Override
    public AbstractTest newInstance() {
        return new GetHomepageTest();
    }
}
