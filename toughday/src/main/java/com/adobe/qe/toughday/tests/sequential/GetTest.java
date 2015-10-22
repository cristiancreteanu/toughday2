package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.config.ConfigArg;

/**
 * Test for performing a get on a URL
 */
public class GetTest extends SequentialTestBase {

    private String path;

    public GetTest() {
        this.path = "/crx/de";
    }

    /**
     * Setter for the name
     */
    @ConfigArg(required = false, desc = "The path to perform a GET on. Default is /crx/de")
    public AbstractTest setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public void test() throws ClientException {
        getDefaultClient().http().doGet(path);
    }

    @Override
    public AbstractTest newInstance() {
        GetTest test = new GetTest();
        test.setPath(this.path);
        return test;
    }
}
