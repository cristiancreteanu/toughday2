package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.Arrays;

/**
 * Created by tuicu on 05/11/16.
 */
@Description(name="create_tag_tree", desc=
        "This test creates tags hierarchically. Each child on each level has 10 children. " +
                "Each author thread fills in a level in the tag tree, up to 10^level")
public class CreateTagTreeTest extends SequentialTestBase {
    public static final Logger LOG = getLogger(CreateFolderTreeTest.class);

    public static final String CREATE_TAG_CMD = "createTag";
    public static final String TAG_COMMAND_URL = "/bin/tagcommand";

    public static final String DEFAULT_NAMESPACE = "ToughDay";
    public static final String DEFAULT_TAG_TITLE = "ToughDay";

    public static final String EXTRA_TAG_TITLE = "ToughDayTag";

    private static final String NAMEPSPACE_DESCRIPTION = "Isolated namespace for ToughDay testing";
    private static final String TAG_DESCRIPTION = "Toughday Tag";

    private TreePhaser phaser;
    private boolean failed;
    private String parentPath;
    private String title; //TODO use this
    private String nodeName;
    private String namespace = DEFAULT_NAMESPACE;
    private int nextChild;
    private String extra_tag;

    public CreateTagTreeTest() {
        phaser = new TreePhaser();
    }

    public CreateTagTreeTest(TreePhaser phaser, String namespace, String title, String extra_tag) {
        this.phaser = phaser;
        this.namespace = namespace;
        this.title = title;
        this.extra_tag = extra_tag;
    }

    @FactorySetup
    private void setup() {
        try {
            String isolatedNameSpace = namespace + "_" + RandomStringUtils.randomAlphanumeric(5);
            createNamespace(isolatedNameSpace, isolatedNameSpace, NAMEPSPACE_DESCRIPTION);
            namespace = isolatedNameSpace;
        } catch (Exception e) {
            LOG.warn("Failed to create namespace {}", namespace);
            namespace = "default";
        }

        //Create one initial tag to be used for adding it to all pages, so we have a very large index.
        try {
            createTag(EXTRA_TAG_TITLE, EXTRA_TAG_TITLE, "ToughDay extra tag", namespace + ":");
            extra_tag = namespace + ":" + EXTRA_TAG_TITLE;
        } catch (Exception e) {
            //TODO
        }
    }

    @Before
    private void before() {
        phaser.register();

        // this gets the next node on the level and potentially waits for other threads to reset the level
        // save those values for later use
        this.nextChild = phaser.getNextNode();
        this.parentPath = namespace + ":" + StringUtils.stripStart(TreePhaser.computeParentPath(nextChild, phaser.getLevel()), "/");
        this.nodeName = TreePhaser.computeNodeName(nextChild);
        this.failed = false;
    }

    @Override
    public void test() throws Exception {
        try {
            createTag();
        } catch (Exception e) {
            this.failed = true;
            // log and throw. It's normally an anti-pattern, but we don't log exceptions anywhere on the upper level,
            // we're just count them.
            LOG.warn("Failed to create tag {}{} ({})", parentPath, nodeName, e.getMessage());
            throw e;
        }
    }

    @After
    private void after() {
        if (LOG.isDebugEnabled()) LOG.debug("In after() tid={}", Thread.currentThread().getId());
        // make sure the page was created
        for (int i=0; i<5; i++) {
            try {
                // If operation was marked as failed and the path really does not exist,
                // try and create it, as it is needed as the parent path for the children on the next level
                if (!failed || getDefaultClient().exists(this.parentPath + nodeName)) {
                    break;
                } else {
                    if (LOG.isDebugEnabled()) LOG.debug("Retrying to create tag tid={} nextChild={} phase={} path={}\n",
                            Thread.currentThread().getId(), nextChild, phaser.getLevel(),
                            parentPath + nodeName);
                    createTag();
                }
            } catch (Exception e) {
                LOG.warn("In after(): Failed to create tag {}{}", parentPath, nodeName);
            }
        }

        // de-register
        phaser.arriveAndDeregister();
    }

    private void createNamespace(String title, String tag, String description) throws Exception {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("jcr:title", title)
                .addParameter("tag", tag)
                .addParameter("jcr:description", description)
                .addParameter("cmd", CREATE_TAG_CMD);
        getDefaultClient().doPost(TAG_COMMAND_URL, feb.build(), HttpStatus.SC_OK);
    }

    private void createTag() throws Exception {
        createTag(nodeName, nodeName, TAG_DESCRIPTION, parentPath);
    }

    private void createTag(String title, String tag, String description, String parentTagID) throws Exception {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("jcr:title", title)
                .addParameter("tag", tag)
                .addParameter("jcr:description", description)
                .addParameter("parentTagID", parentTagID)
                .addParameter("cmd", CREATE_TAG_CMD);
        getDefaultClient().doPost(TAG_COMMAND_URL, feb.build(), HttpStatus.SC_OK);
        communicate("tags", Arrays.asList(extra_tag, parentTagID + title));
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateTagTreeTest(phaser, namespace, title, extra_tag);
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_NAMESPACE,
            desc = "The title of the namespace where the tags will be created. A random string will be added internally to make it unique")
    public AbstractTest setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_NAMESPACE,
            desc = "The title of the tags. Internally, this will be incremented")
    public AbstractTest setTitle(String title) {
        this.title = title;
        return this;
    }
}
