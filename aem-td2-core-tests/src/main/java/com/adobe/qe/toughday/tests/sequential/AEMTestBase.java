package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.api.core.SequentialTest;
import com.adobe.qe.toughday.api.core.config.GlobalArgs;
import com.adobe.qe.toughday.tests.utils.SlingClientsProxyFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.interceptors.FormBasedAuthInterceptor;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base class for sequential tests.
 */
public abstract class AEMTestBase extends SequentialTest {
    private SlingClient defaultClient;
    public AEMTestBase() {
        benchmark().registerHierarchyProxyFactory(SlingClient.class, new SlingClientsProxyFactory());
    }

    public SlingClient getDefaultClient() throws Exception {
        if (defaultClient == null) {
            defaultClient = AEMTestBase.createClient(getGlobalArgs());
        }
        return defaultClient;
    }


    public static SlingClient createClient (GlobalArgs args) throws URISyntaxException, ClientException {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(args.getProtocol())
                .setHost(args.getHost())
                .setPort(args.getPort());

        if(args.getContextPath() != null) {
            URI cp = URI.create("/").resolve(args.getContextPath());
            uriBuilder.setPath(cp.getPath());
        }

        SlingClient.Builder builder = SlingClient.Builder.create(uriBuilder.build(),
                args.getUser(),
                args.getPassword());
        if(args.getAuthMethod().equals("basic")) {
            //no-op
        } else if(args.getAuthMethod().equals("login-token")) {
            Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().build();
            builder.httpClientBuilder().setDefaultAuthSchemeRegistry(authSchemeRegistry);
            builder.setPreemptiveAuth(false).addInterceptorFirst(new FormBasedAuthInterceptor("login-token"));
        } else {
            throw new IllegalArgumentException("Unsupported authentication method: " + args.getAuthMethod());
        }

        return builder.build();
    }
}
