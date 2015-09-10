package com.day.qa.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.client.GraniteClient;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.cli.CliArg;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreatePageTest extends SequentialTestBase {
    private String parentPath;
    private String template;
    static AtomicInteger pageNumber = new AtomicInteger(0);

    public CreatePageTest() {
    }

    protected CreatePageTest(String parentPath, String template) {
        this.parentPath = parentPath;
        this.template = template;
    }

    public static final String CMD_CREATE_PAGE = "createPage";
    //public static final String PARENT_PATH = "/content/geometrixx-outdoors/en/men";
    //public static final String TEMPLATE = "/apps/geometrixx-outdoors/templates/page";

    @Override
    public void test() throws ClientException {
        GraniteClient client = getDefaultClient();

        int nextPage = pageNumber.getAndIncrement();
        FormEntityBuilder feb = new FormEntityBuilder().addParameter("cmd", CMD_CREATE_PAGE)
                                    .addParameter("parentPath", parentPath)
                                    .addParameter("title", "Page" + nextPage)
                                    .addParameter("template", template);

        RequestExecutor req = client.http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest(parentPath, template);
    }

    @CliArg
    public AbstractTest setParentPath(String parentPath) {
        this.parentPath = parentPath;
        return this;
    }

    @CliArg
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }
}
