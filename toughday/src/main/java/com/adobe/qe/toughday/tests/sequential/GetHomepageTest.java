package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import org.apache.http.HttpStatus;

/**
 * Test for getting the homepage.
 */
public class GetHomepageTest extends SequentialTestBase {

    @Override
    public void test() throws Exception {
        getDefaultClient().doGet("/", HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new GetHomepageTest();
    }
}
