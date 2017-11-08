package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Tag;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.SkippedTestException;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.concurrent.atomic.AtomicInteger;

@Tag(tags = { "author" })
@Description(desc = "Test for deleting pages.")
public class DeletePageTest extends SequentialTestBase {

    private static final String CMD_DELETE_PAGE = "deletePage";
    private String parentPath = CreatePageTest.DEFAULT_PARENT_PATH;
    private String title = AuthoringTest.DEFAULT_PAGE_TITLE;
    private boolean force = true;

    public DeletePageTest() {
    }

    public DeletePageTest(String parentPath, boolean force, String title) {
        this.parentPath = parentPath;
        this.force = force;
        this.title = title;
    }

    protected String getNextTitle() throws SkippedTestException {
        return CreatePageTest.lastCreated.get();
    }

    @Override
    public void test() throws Throwable {
        String nextTitle = getNextTitle();
        if (nextTitle == null) {
            throw new SkippedTestException(new ClientException("No page created (by CreatePageTest). Marking as skipped."));
        }

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_DELETE_PAGE)
                .addParameter("force", Boolean.valueOf(force).toString())
                .addParameter("shallow", Boolean.toString(false))
                .addParameter("path", parentPath + "/" + nextTitle);

        try {
            logger().debug("{}: Trying to delete={}{}", Thread.currentThread().getName(), parentPath, title);

            getDefaultClient().doPost("/bin/wcmcommand", feb.build(), HttpStatus.SC_OK);

        } catch (Throwable e) {
            logger().warn("{}: Failed to delete={}{}", Thread.currentThread().getName(), parentPath, title);
            logger().debug(Thread.currentThread().getName() + ": ERROR: ", e);

            throw e;
        }

        logger().debug("{}: Successfully deleted={}{}", Thread.currentThread().getName(), parentPath, title);
    }



    // Test params

    @Override
    public AbstractTest newInstance() {
        return new DeletePageTest(parentPath, force, title);
    }

    @ConfigArgSet(required = false, defaultValue = CreatePageTest.DEFAULT_PARENT_PATH,
            desc = "The parent path of the page to be deleted. E.g. The one created by CreatePageTest")
    public DeletePageTest setParentPath(String parentPath) {
        this.parentPath = (parentPath.endsWith("/") ? parentPath : parentPath + "/") ;
        return this;
    }

    @ConfigArgSet(required = false, defaultValue = "true", desc = "true/ false; Whether to force delete the page.")
    public DeletePageTest setForce(String force) {
        this.force = Boolean.parseBoolean(force);
        return this;
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page to be deleted. e.g. The one from CreatePageTest")
    public DeletePageTest setTitle(String title) {
        this.title = title;
        return this;
    }

    public DeletePageTest setNext(AtomicInteger next) {
        return this;
    }

    @ConfigArgGet
    public boolean getForce() {
        return force;
    }

    @ConfigArgGet
    public String getTitle() {
        return title;
    }

    @ConfigArgGet
    public String getParentPath() {
        return parentPath;
    }

}
