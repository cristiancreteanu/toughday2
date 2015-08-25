package com.day.qa.toughday.tests;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.client.GraniteClient;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.day.qa.toughday.tests.annotations.Setup;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreatePageTest extends AbstractTest {
    GraniteClient client;
    static AtomicInteger pageNumber = new AtomicInteger(0);


    public static final String CMD_CREATE_PAGE = "createPage";
    public static final String PARENT_PATH = "/content/geometrixx-outdoors/en/men";
    public static final String TEMPLATE = "/apps/geometrixx-outdoors/templates/page";

    @Override
    public void test() throws ClientException {
        int nextPage = pageNumber.getAndIncrement();
        FormEntityBuilder feb = new FormEntityBuilder().addParameter("cmd", CMD_CREATE_PAGE)
                                    .addParameter("parentPath", PARENT_PATH)
                                    .addParameter("title", "Page" + nextPage)
                                    .addParameter("template", TEMPLATE);

        RequestExecutor req = client.http().doPost("/bin/wcmcommand", feb.getEntity());
        if(req.getResponse().getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new ClientException("Expected status code 200, but got " + req.getResponse().getStatusLine().getStatusCode());
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest();
    }

    @Setup
    public void setup() {
        this.client = new GraniteClient("http://localhost:4502", "admin", "admin");
    }
}
