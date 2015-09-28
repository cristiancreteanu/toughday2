package com.day.qa.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.client.GraniteClient;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.AbstractTestRunner;
import com.day.qa.toughday.runners.SequentialTestRunner;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tuicu on 04/09/15.
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
        if(defaultClient == null) {
            defaultClient = new GraniteClient(getGlobalArgs().getHost() + ":" + getGlobalArgs().getPort(),
                                    getGlobalArgs().getUser(),
                                    getGlobalArgs().getPassword());

        }
        return defaultClient;
    }

    public void checkStatus(int receivedStatus, int expectedStatus) throws ClientException{
        if(receivedStatus != expectedStatus) {
            throw new ClientException("Expected status code 200, but got " + receivedStatus);
        }
    }

    public abstract void test() throws Exception;
}
