package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class CreatePageTest extends SequentialTestBase {
    private String parentPath = DEFAULT_PARENT_PATH;
    private String template = DEFAULT_TEMPLATE;
    private String title = AuthoringTest.DEFAULT_PAGE_TITLE;

    public static ConcurrentHashMap<Thread, String> lastCreated = new ConcurrentHashMap<Thread, String>();

    public CreatePageTest() {
    }

    protected CreatePageTest(String parentPath, String template, String title) {
        this.parentPath = parentPath;
        this.template = template;
        this.title = title;
    }

    public static final AtomicInteger nextNumber = new AtomicInteger(0);
    public static final String CMD_CREATE_PAGE = "createPage";
    public static final String DEFAULT_PARENT_PATH = "/content/geometrixx/en";
    public static final String DEFAULT_TEMPLATE = "/apps/geometrixx/templates/contentpage";

    @Override
    public void test() throws ClientException {

        String nextTitle = title + nextNumber.getAndIncrement();
        lastCreated.put(Thread.currentThread(), nextTitle);

        FormEntityBuilder feb = new FormEntityBuilder()
                .addParameter("cmd", CMD_CREATE_PAGE)
                .addParameter("parentPath", parentPath)
                .addParameter("title", nextTitle)
                .addParameter("template", template);

        RequestExecutor req = getDefaultClient().http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest(parentPath, template, title);
    }

    @ConfigArg(required = false)
    public AbstractTest setTitle(String title) {
        this.title = title.toLowerCase();
        return this;
    }

    @ConfigArg(required = false)
    public AbstractTest setParentPath(String parentPath) {
        this.parentPath = parentPath;
        return this;
    }

    @ConfigArg(required = false)
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }
}
