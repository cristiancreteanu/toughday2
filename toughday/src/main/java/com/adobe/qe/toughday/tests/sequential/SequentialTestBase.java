package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.AbstractTestRunner;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.runners.SequentialTestRunner;
import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    public SlingClient getDefaultClient() throws Throwable {
        if (defaultClient == null) {
            defaultClient = SequentialTestBase.createClient(getGlobalArgs());
        }
        return defaultClient;
    }

    public abstract void test() throws Throwable;


    public static SlingClient createClient (Configuration.GlobalArgs args) throws URISyntaxException, ClientException {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(args.getProtocol())
                .setHost(args.getHost())
                .setPort(args.getPort());

        if(args.getContextPath() != null) {
            URI cp = URI.create("/").resolve(args.getContextPath());
            uriBuilder.setPath(cp.getPath());
        }

        return new SlingClient(uriBuilder.build(),
                args.getUser(),
                args.getPassword());
    }
}
