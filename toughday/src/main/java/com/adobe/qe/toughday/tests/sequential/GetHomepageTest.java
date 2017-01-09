package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Description;
import org.apache.http.HttpStatus;

@Description(desc = "GET the home page.")
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
