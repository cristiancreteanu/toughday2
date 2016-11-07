package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.ChildTestFailedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.List;

/**
 * Created by tuicu on 05/11/16.
 * TODO: Make this invisible to help. It's just an utility test, should not be run by itself.
 */
public class AddTagToResourceTest extends SequentialTestBase {

    @Override
    public void test() throws Exception {
        String resourcePath = getCommunication("resource", null);
        List<String> tags = getCommunication("tags", null);

        if(resourcePath == null || tags == null) {
            throw new IllegalStateException("Either the resource or the tags were missing");
        }
        resourcePath = resourcePath.endsWith("_jcr_content") ? resourcePath : StringUtils.stripEnd(resourcePath, "/") + "/_jcr_content";

        FormEntityBuilder builder = FormEntityBuilder.create();

        for(String tag : tags) {
            builder.addParameter("./cq:tags", tag);
        }

        builder.addParameter("./cq:tags@TypeHint", "String[]");

        getDefaultClient().doPost(resourcePath, builder.build(), HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new AddTagToResourceTest();
    }
}
