package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Tag;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.samplecontent.SampleContent;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.concurrent.atomic.AtomicInteger;

@Tag(tags = { "author" })
@Description(desc = "This test creates pages under the same parent path." +
        " Due to OAK limitations, performance will decrease over time." +
        " If you are not looking for this specific scenario, please consider using CreatePageTreeTest.")
public class CreatePageTest extends SequentialTestBase {
    public static final Logger LOG = createLogger(CreatePageTest.class);

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
    public static final String DEFAULT_PARENT_PATH = SampleContent.TOUGHDAY_SITE;
    public static final String DEFAULT_TEMPLATE = SampleContent.TOUGHDAY_TEMPLATE;

    @Override
    public void test() throws Exception {

        String nextTitle = title + nextNumber.getAndIncrement();
        lastCreated.set(nextTitle);

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_CREATE_PAGE)
                .addParameter("parentPath", rootParentPath)
                .addParameter("title", nextTitle)
                .addParameter("template", template);

        try {
            LOG.debug("{}: Trying to create page={}{}, with template={}", Thread.currentThread().getName(), rootParentPath, nextTitle, template);

            getDefaultClient().doPost("/bin/wcmcommand", feb.build(), HttpStatus.SC_OK);
        } catch (Throwable e) {
            LOG.warn("{}: Failed to create page={}{}, with template={}", Thread.currentThread().getName(), rootParentPath, nextTitle, template);
            LOG.debug(Thread.currentThread().getName() + "ERROR: ", e);

            throw e;
        }

        LOG.debug("{}: Successfully created page={}{}, with template={}", Thread.currentThread().getName(), rootParentPath, nextTitle, template);
    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePageTest(rootParentPath, template, title);
    }


    @ConfigArgSet(required = false, defaultValue = AuthoringTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AbstractTest setTitle(String title) {
        this.title = title.toLowerCase();
        return this;
    }

    @ConfigArgGet
    public String getTitle() {
        return this.title;
    }

    @ConfigArgSet(required = false, defaultValue = DEFAULT_PARENT_PATH,
            desc = "The path prefix for all pages.")
    public AbstractTest setParentPath(String parentPath) {
        this.rootParentPath = StringUtils.stripEnd(parentPath, "/");
        return this;
    }

    @ConfigArgGet
    public String getParentPath() {
        return this.rootParentPath;
    }

    @ConfigArgSet(required = false, defaultValue = DEFAULT_TEMPLATE)
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }

    @ConfigArgGet
    public String getTemplate() {
        return this.template;
    }

}
