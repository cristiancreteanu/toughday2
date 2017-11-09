package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.api.core.AbstractTestRunner;
import com.adobe.qe.toughday.api.core.SequentialTest;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.runners.SequentialTestRunner;
import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base class for sequential tests.
 */
public abstract class SequentialTestBase extends SequentialTest {
    private SlingClient defaultClient;

    public SlingClient getDefaultClient() throws Exception {
        if (defaultClient == null) {
            defaultClient = SequentialTestBase.createClient(getGlobalArgs());
        }
        return defaultClient;
    }


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
