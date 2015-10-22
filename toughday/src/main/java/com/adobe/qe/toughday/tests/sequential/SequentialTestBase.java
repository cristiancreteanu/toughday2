package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.client.GraniteClient;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.runners.SequentialTestRunner;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for sequential tests.
 */
public abstract class SequentialTestBase extends AbstractTest {
    private static final List<AbstractTest> noChildren = new ArrayList<>();
    private GraniteClient defaultClient;


    @Override
    public List<AbstractTest> getChildren() {
        return noChildren;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return SequentialTestRunner.class;
    }

    public GraniteClient getDefaultClient() {
        if (defaultClient == null) {
            defaultClient = new GraniteClient("http://" + getGlobalArgs().getHost() + ":" + getGlobalArgs().getPort(),
                                    getGlobalArgs().getUser(),
                                    getGlobalArgs().getPassword());

        }
        return defaultClient;
    }

    public void checkStatus(int receivedStatus, int expectedStatus) throws ClientException{
        if (receivedStatus != expectedStatus) {
            throw new ClientException("Expected status code 200, but got " + receivedStatus);
        }
    }

    public abstract void test() throws Exception;
}
