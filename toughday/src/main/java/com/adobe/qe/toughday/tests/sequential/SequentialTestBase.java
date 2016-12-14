package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.runners.SequentialTestRunner;
import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.ClientException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * Base class for sequential tests.
 */
public abstract class SequentialTestBase extends AbstractTest {
    private static final List<AbstractTest> noChildren = new ArrayList<>();
    private SlingClient defaultClient;


    @Override
    public List<AbstractTest> getChildren() {
        return noChildren;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return SequentialTestRunner.class;
    }

    public SlingClient getDefaultClient() throws Exception {
        if (defaultClient == null) {
            URI uri = new URIBuilder()
                    .setScheme(getGlobalArgs().getProtocol())
                    .setHost(getGlobalArgs().getHost())
                    .setPort(getGlobalArgs().getPort())
                    .build();

            defaultClient = new SlingClient(uri,
                        getGlobalArgs().getUser(),
                        getGlobalArgs().getPassword());
        }
        return defaultClient;
    }

    public abstract void test() throws Exception;
}
