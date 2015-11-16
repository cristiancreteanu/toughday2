package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.config.ConfigArg;

/**
 * Test for performing a get on a URL
 */
public class GetTest extends SequentialTestBase {
    public static final String DEFAULT_PATH = "/crx/de";
    private String path;


    public GetTest() {
        this.path = DEFAULT_PATH;
    }

    /**
     * Setter for the name
     */
    @ConfigArg(required = false, defaultValue = DEFAULT_PATH, desc = "The path at which to perform a GET request")
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
