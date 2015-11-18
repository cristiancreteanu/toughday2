package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class CreatePageTest extends SequentialTestBase {
    public static final Logger LOG = getLogger(CreatePageTest.class);

    private String rootParentPath = DEFAULT_PARENT_PATH;
    private String template = DEFAULT_TEMPLATE;
    private String title = AuthoringTest.DEFAULT_PAGE_TITLE;

    public static ThreadLocal<String> lastCreated = new ThreadLocal<String>();

    public CreatePageTest() {
    }

    protected CreatePageTest(String parentPath, String template, String title) {
        this.rootParentPath = parentPath;
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
        lastCreated.set(nextTitle);

        FormEntityBuilder feb = new FormEntityBuilder()
                .addParameter("cmd", CMD_CREATE_PAGE)
                .addParameter("parentPath", rootParentPath)
                .addParameter("title", nextTitle)
                .addParameter("template", template);

        RequestExecutor req = getDefaultClient().http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest(rootParentPath, template, title);
    }


    @ConfigArg(required = false, defaultValue = AuthoringTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AbstractTest setTitle(String title) {
        this.title = title.toLowerCase();
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_PARENT_PATH,
            desc = "The path prefix for all pages.")
    public AbstractTest setParentPath(String parentPath) {
        this.rootParentPath = StringUtils.stripEnd(parentPath, "/");
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_TEMPLATE)
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }


}
