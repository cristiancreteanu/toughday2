package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Tag;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.tests.sequential.image.UploadImageTest;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;

@Tag(tags = { "author", "publish" })
@Description(desc = "GET a specific page.")
public class GetTest extends SequentialTestBase {
    public static Logger LOG = createLogger(GetTest.class);

    public static final String DEFAULT_PATH = "/crx/de";
    private String path;


    public GetTest() {
        this.path = DEFAULT_PATH;
    }

    /**
     * Setter for the name
     */
    @ConfigArgSet(required = false, defaultValue = DEFAULT_PATH, desc = "The path at which to perform a GET request")
    public AbstractTest setPath(String path) {
        this.path = path;
        return this;
    }

    @ConfigArgGet
    public String getPath() {
        return this.path;
    }

    @Override
    public void test() throws Exception {
        try {
            LOG.debug("{}: Trying to GET path={}", Thread.currentThread().getName(), path);

            getDefaultClient().doGet(path, HttpStatus.SC_OK);
        } catch (Throwable e) {
            LOG.warn("{}: Failed to GET path={}", Thread.currentThread().getName(), path);
            LOG.debug(Thread.currentThread().getName() + ": ERROR :", e);

            throw e;
        }

        LOG.debug("{}: Successfully did GET path={}", Thread.currentThread().getName(), path);
    }

    @Override
    public AbstractTest newInstance() {
        GetTest test = new GetTest();
        test.setPath(this.path);
        return test;
    }
}
