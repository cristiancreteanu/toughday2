package com.day.qa.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.cli.CliArg;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 10/09/15.
 */
public class DeletePageTest extends SequentialTestBase {
    private static final String CMD_DELETE_PAGE = "deletePage";
    private String parentPath;
    private String title;
    private boolean force;
    private AtomicInteger next;

    public DeletePageTest(){
    }

    public DeletePageTest(String parentPath, boolean force, String title, AtomicInteger next) {
        this.parentPath = parentPath;
        this.force = force;
        this.next = next;
        this.title = title;
    }

    @Override
    public void test() throws ClientException {
        int nextNumber = next.getAndDecrement();
        if(nextNumber == 0)
            return;
        FormEntityBuilder feb = new FormEntityBuilder().addParameter("cmd", CMD_DELETE_PAGE)
                .addParameter("force", Boolean.valueOf(force).toString())
                .addParameter("shallow", Boolean.toString(false))
                .addParameter("path", parentPath + title + nextNumber);

        RequestExecutor executor = getDefaultClient().http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(executor.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new DeletePageTest(parentPath, force, title, next);
    }

    @CliArg
    public DeletePageTest setParentPath(String parentPath) {
        this.parentPath = (parentPath.endsWith("/") ? parentPath : parentPath + "/") ;
        return this;
    }

    @CliArg
    public DeletePageTest setForce(String force) {
        this.force = Boolean.parseBoolean(force);
        return this;
    }

    @CliArg
    public DeletePageTest setTitle(String title) {
        this.title = title;
        return this;
    }

    public DeletePageTest setNext(AtomicInteger next) {
        this.next = next;
        return this;
    }

}
