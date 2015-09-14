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
    private String title;
    private AtomicInteger nextPageNumber;

    public static AtomicInteger defaultNextPageNumber = new AtomicInteger(0);

    public CreatePageTest() {
        nextPageNumber = defaultNextPageNumber;
    }

    protected CreatePageTest(String parentPath, String template, String title, AtomicInteger nextPageNumber) {
        this.parentPath = parentPath;
        this.template = template;
        this.title = title;
        this.nextPageNumber = nextPageNumber;
    }

    public static final String CMD_CREATE_PAGE = "createPage";
    public static final String DEFAULT_PARENT_PATH = "/content/geometrixx-outdoors/en/men";
    public static final String DEFAULT_TEMPLATE = "/apps/geometrixx-outdoors/templates/page";

    @Override
    public void test() throws ClientException {
        GraniteClient client = getDefaultClient();

        int nextPage = nextPageNumber.incrementAndGet();
        FormEntityBuilder feb = new FormEntityBuilder().addParameter("cmd", CMD_CREATE_PAGE)
                                    .addParameter("parentPath", parentPath)
                                    .addParameter("title", title + nextPage)
                                    .addParameter("template", template);

        RequestExecutor req = client.http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest(parentPath, template, title, nextPageNumber);
    }

    @CliArg
    public AbstractTest setTitle(String title) {
        this.title = title;
        return this;
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

    public AbstractTest setLastPageNumber(AtomicInteger nextPageNumber) {
        this.nextPageNumber = nextPageNumber;
        return this;
    }

    public AtomicInteger getNextPageNumber() {
        return nextPageNumber;
    }
}
