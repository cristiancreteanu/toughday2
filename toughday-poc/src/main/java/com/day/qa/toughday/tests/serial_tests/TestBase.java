package com.day.qa.toughday.tests.serial_tests;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.client.GraniteClient;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.core.GlobalArgs;
import com.day.qa.toughday.runners.TestRunner;


/**
 * Created by tuicu on 04/09/15.
 */
public abstract class TestBase extends AbstractTest {
    private GraniteClient defaultClient;

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return TestRunner.class;
    }

    public GraniteClient getDefaultClient() {
        if(defaultClient == null) {
            defaultClient = new GraniteClient(GlobalArgs.getInstance().getHost() + ":" + GlobalArgs.getInstance().getPort(),
                                    GlobalArgs.getInstance().getUser(),
                                    GlobalArgs.getInstance().getPassword());

        }
        return defaultClient;
    }

    public void checkStatus(int receivedStatus, int expectedStatus) throws ClientException{
        if(receivedStatus != expectedStatus) {
            throw new ClientException("Expected status code 200, but got " + receivedStatus);
        }
    }

    public abstract void test() throws ClientException;
}
