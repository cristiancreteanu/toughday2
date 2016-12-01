package com.adobe.qe.toughday.tests.sequential.users;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.commons.html.impl.HtmlParserImpl;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateUserGroupTest extends SequentialTestBase {
    private String id;
    private String groupName = DEFAULT_GROUP_NAME;
    private String description = DEFAULT_GROUP_DESCRIPTION;
    private AtomicInteger increment;

    private String extraGroup; //A group created before running the tests the will be communicated.

    public static final String DEFAULT_GROUP_NAME = "ToughDay";
    public static final String DEFAULT_GROUP_DESCRIPTION = "A group for ToughDay users";

    public CreateUserGroupTest() {
        increment = new AtomicInteger(0);
    }

    public CreateUserGroupTest(AtomicInteger increment, String groupName, String description, String extraGroup) {
        this.groupName = groupName;
        this.description = description;
        this.increment = increment;
    }

    @FactorySetup
    private void setup() {
        try {
            extraGroup = createGroup(getDefaultClient(), id, groupName, description);
        } catch (Exception e) {
            //TODO
            extraGroup = null;
        }
    }

    @Before
    private void before() {
        id = RandomStringUtils.randomAlphanumeric(20);
    }

    @Override
    public void test() throws Exception {
        String groupName = this.groupName;
        if(increment != null) {
            groupName += increment.getAndIncrement();
        }

        String groupPath = createGroup(getDefaultClient(), id, groupName, description);

        communicate("groups", extraGroup != null ? Arrays.asList(extraGroup, groupPath) : Arrays.asList(groupPath));
    }

    /**
     * Create a group
     * @return path to the created group
     */
    public static String createGroup(SlingClient client, String id, String groupName, String description) throws Exception {
        HtmlParserImpl htmlParser = htmlParser = new HtmlParserImpl();
        FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                .addParameter("authorizableId", id)
                .addParameter("./profile/givenName", groupName)
                .addParameter("./profile/aboutMe", description)
                .addParameter("createGroup", "1")
                .addParameter("_charset_", "utf-8");

        SlingHttpResponse response = client.doPost("/libs/granite/security/post/authorizables.html", entityBuilder.build(), HttpStatus.SC_CREATED);
        Document responseHtml = htmlParser.parse(null, IOUtils.toInputStream(response.getContent()), "utf-8");
        return responseHtml.getElementsByTagName("title").item(0).getTextContent().split(" ")[2];
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateUserGroupTest(increment, groupName, description, extraGroup);
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_GROUP_NAME)
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_GROUP_DESCRIPTION)
    public void setDescription(String description) {
        this.description = description;
    }


    @ConfigArg(required = false, desc = "Increment the group name", defaultValue = "true")
    public void setIncrement(String value) {
        if(!Boolean.valueOf(value))
            increment = null;
    }
}
