package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
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
    public static final String ROOT_NODE = "toughday";
    public static final String ROOT_NODE_PATH ="/content/" + ROOT_NODE ;
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

    @FactorySetup
    private void prepareContent() throws Exception {
        if (!getDefaultClient().exists(ROOT_NODE_PATH)) {
            getDefaultClient().createFolder(ROOT_NODE, ROOT_NODE, "/content");
        }
    }

    public abstract void test() throws Exception;
}
