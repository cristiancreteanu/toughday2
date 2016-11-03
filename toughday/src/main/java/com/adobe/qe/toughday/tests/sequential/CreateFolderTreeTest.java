package com.adobe.qe.toughday.tests.sequential;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.*;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.composite.AuthoringTreeTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

/**
 *
 */
@Description(name="create_folder_tree", desc=
        "This test creates folders hierarchically. Each child on each level has 10 children. " +
                "Each author thread fills in a level in the folder tree, up to 10^level")
public class CreateFolderTreeTest extends SequentialTestBase {
    public static final Logger LOG = getLogger(CreatePageTreeTest.class);

    private static final String FOLDER_RESOURCE_TYPE = "sling:Folder";
    private static final String UNSTRUCTURED_RESOURCE_TYPE = "nt:unstructured";
    public static final String DEFAULT_PARENT_PATH = "/content/dam";

    // needed for synchronizing
    private TreePhaser phaser;

    private String rootParentPath = DEFAULT_PARENT_PATH;
    private String title = AuthoringTreeTest.DEFAULT_PAGE_TITLE;

    private int nextChild;

    private String parentPath;
    private String nodeName;
    private boolean failed = false;

    public CreateFolderTreeTest() {
        phaser = new TreePhaser();
        AbstractTest.addExtraThread(phaser.mon);

    }

    protected CreateFolderTreeTest(TreePhaser phaser, String parentPath, String title) {
        this.phaser = phaser;
        this.rootParentPath = parentPath;
        this.title = title;
    }

    @FactorySetup
    private void setup() {
        try {
            String isolatedRoot = "tree_" + RandomStringUtils.randomAlphanumeric(5);
            createFolder(isolatedRoot, rootParentPath + "/");
            rootParentPath = rootParentPath + "/" + isolatedRoot;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    private void before() {
        phaser.register();

        // this gets the next node on the level and potentially waits for other threads to reset the level
        // save those values for later use
        this.nextChild = phaser.getNextNode();
        this.parentPath = rootParentPath + TreePhaser.computeParentPath(nextChild, phaser.getLevel());
        this.nodeName = TreePhaser.computeNodeName(nextChild);
        this.failed = false;
    }

    @Override
    public void test() throws Exception {
        try {
            createFolder();
        } catch (Exception e) {
            this.failed = true;
            // log and throw. It's normally an anti-pattern, but we don't log exceptions anywhere on the upper level,
            // we're just count them.
            LOG.warn("Failed to create page {}{} ({})", parentPath, nodeName, e.getMessage());
            throw e;
        }
        if (LOG.isDebugEnabled()) LOG.debug("tid=%{} nextChild={} level={} path={}",
                Thread.currentThread().getId(), nextChild, phaser.getLevel(), parentPath + nodeName);
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
                    if (LOG.isDebugEnabled()) LOG.debug("Retrying to create page tid={} nextChild={} phase={} path={}\n",
                            Thread.currentThread().getId(), nextChild, phaser.getLevel(),
                            parentPath + nodeName);
                    createFolder();
                }
            } catch (Exception e) {
                LOG.warn("In after(): Failed to create page {}{}", parentPath, nodeName);
            }
        }
        communicate("parentPath", parentPath);
        // de-register
        phaser.arriveAndDeregister();
    }

    private void createFolder() throws Exception {
        createFolder(nodeName, parentPath);
    }

    private void createFolder(String nodeName, String parentPath) throws Exception {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter(":name", nodeName)
                .addParameter("./jcr:content/jcr:title", nodeName)
                .addParameter("./jcr:primaryType", FOLDER_RESOURCE_TYPE)
                .addParameter("./jcr:content/jcr:primaryType", UNSTRUCTURED_RESOURCE_TYPE);
        getDefaultClient().doPost(parentPath, feb.build(), HttpStatus.SC_CREATED);
    }


    @Override
    public AbstractTest newInstance() {
        return new CreateFolderTreeTest(phaser, rootParentPath, title);
    }

    @ConfigArg(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AbstractTest setTitle(String title) {
        this.title = title.toLowerCase();
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_PARENT_PATH,
            desc = "The path prefix for all pages.")
    public AbstractTest setParentPath(String parentPath) {
        this.rootParentPath = StringUtils.stripEnd(parentPath, "/");
        return this;
    }
}
