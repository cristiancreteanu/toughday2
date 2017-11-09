package com.adobe.qe.toughday.tests.sequential.tags;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.Internal;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.List;

@Internal
public class AddTagToResourceTest extends SequentialTestBase {

    @Override
    public void test() throws Throwable {
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

        try {
            logger().debug("{}: Trying to add tags to the resource", Thread.currentThread().getName());
            getDefaultClient().doPost(resourcePath, builder.build(), HttpStatus.SC_OK);
        } catch (Throwable e) {
            logger().warn("{}: Failed to add tags to the resource", Thread.currentThread().getName());
            logger().debug(Thread.currentThread().getName() + "ERROR: ", e);

            throw e;
        }
        logger().debug("{}: Successfully added tags to the resource", Thread.currentThread().getName());
    }

    @Override
    public AbstractTest newInstance() {
        return new AddTagToResourceTest();
    }
}
