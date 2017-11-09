package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.Tag;
import org.apache.http.HttpStatus;

@Tag(tags = { "author", "publish" })
@Description(desc = "GET the home page.")
public class GetHomepageTest extends SequentialTestBase {

    @Override
    public void test() throws Throwable {
        getDefaultClient().doGet("/", HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new GetHomepageTest();
    }
}
