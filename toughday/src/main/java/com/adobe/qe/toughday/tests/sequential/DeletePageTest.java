package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
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

    protected String getNextTitle() {
        return CreatePageTest.lastCreated.get(Thread.currentThread());
    }

    @Override
    public void test() throws ClientException {
        String nextTitle = getNextTitle();
        if (nextTitle == null)
            throw new ClientException("No page created (by CreatePageTest). Marking as fail.");

        FormEntityBuilder feb = new FormEntityBuilder().addParameter("cmd", CMD_DELETE_PAGE)
                .addParameter("force", Boolean.valueOf(force).toString())
                .addParameter("shallow", Boolean.toString(false))
                .addParameter("path", parentPath + nextTitle);

        RequestExecutor executor = getDefaultClient().http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(executor.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new DeletePageTest(parentPath, force, title);
    }

    @ConfigArg(required = false)
    public DeletePageTest setParentPath(String parentPath) {
        this.parentPath = (parentPath.endsWith("/") ? parentPath : parentPath + "/") ;
        return this;
    }

    @ConfigArg(required = false)
    public DeletePageTest setForce(String force) {
        this.force = Boolean.parseBoolean(force);
        return this;
    }

    @ConfigArg(required = false)
    public DeletePageTest setTitle(String title) {
        this.title = title;
        return this;
    }

    public DeletePageTest setNext(AtomicInteger next) {
        return this;
    }

    public boolean getForce() {
        return force;
    }

    public String getTitle() {
        return title;
    }

    public String getParentPath() {
        return parentPath;
    }

}
