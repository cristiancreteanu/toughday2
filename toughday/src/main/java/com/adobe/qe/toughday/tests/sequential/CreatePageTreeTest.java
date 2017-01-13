package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.*;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.samplecontent.SampleContent;
import com.adobe.qe.toughday.tests.composite.AuthoringTreeTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.UUID;

/**
 *
 */
@SuppressWarnings("Duplicates")
@Description(desc=
                "This test creates pages hierarchically. Each child on each level has \"base\" children. " +
                "Each author thread fills in a level in the pages tree, up to base^level")
public class CreatePageTreeTest extends SequentialTestBase {
    public static final Logger LOG = createLogger(CreatePageTreeTest.class);

    private final TreePhaser phaser;

    public String rootParentPath = SampleContent.TOUGHDAY_SITE;
    private String template = SampleContent.TOUGHDAY_TEMPLATE;
    private String title = AuthoringTreeTest.DEFAULT_PAGE_TITLE;

    private Integer nextChild;

    private String parentPath;
    private String nodeName;
    private boolean failed = false;

    public CreatePageTreeTest() {
        phaser = new TreePhaser();
    }

    protected CreatePageTreeTest(TreePhaser phaser, String parentPath, String template, String title) {
        this.phaser = phaser;
        this.rootParentPath = parentPath;
        this.template = template;
        this.title = title;
    }

    @FactorySetup
    private void setupContent() {
        String isolatedFolder = "toughday" + UUID.randomUUID();
        try {
            getDefaultClient().createFolder(isolatedFolder, isolatedFolder, rootParentPath);
            rootParentPath = rootParentPath + "/" + isolatedFolder;
        } catch (Exception e) {
            LOG.debug("Could not create isolated folder for running " + getFullName());
        }
    }

    @Before
    private void setup() {
        phaser.register();

        // this gets the next node on the level and potentially waits for other threads to reset the level
        // save those values for later use
        this.nextChild = phaser.getNextNode();
        this.parentPath = TreePhaser.computeParentPath(nextChild, phaser.getLevel(), phaser.getBase(), title, rootParentPath);
        this.nodeName = TreePhaser.computeNodeName(nextChild, phaser.getBase(), title);
        this.failed = false;
    }

    @Override
    public void test() throws Exception {
        try {
            createPage();
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
                    createPage();
                }
            } catch (Exception e) {
                LOG.warn("In after(): Failed to create page {}{}", parentPath, nodeName);
            }
        }

        // de-register
        phaser.arriveAndDeregister();
    }

    private void createPage() throws Exception {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", WcmUtils.CMD_CREATE_PAGE)
                .addParameter("parentPath", parentPath)
                .addParameter("label", nodeName)
                .addParameter("title", title)
                .addParameter("template", template);

        getDefaultClient().doPost("/bin/wcmcommand", feb.build(), HttpStatus.SC_OK);
        communicate("resource", parentPath + nodeName);
    }


    @Override
    public AbstractTest newInstance() {
        return new CreatePageTreeTest(phaser, rootParentPath, template, title);
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AbstractTest setTitle(String title) {
        this.title = title.toLowerCase();
        return this;
    }

    @ConfigArgGet
    public String getTitle() {
        return this.title;
    }

    @ConfigArgSet(required = false, defaultValue = SampleContent.TOUGHDAY_SITE,
            desc = "The path prefix for all pages.")
    public AbstractTest setParentPath(String parentPath) {
        this.rootParentPath = StringUtils.stripEnd(parentPath, "/");
        return this;
    }

    @ConfigArgGet
    public String getParentPath() {
        return this.rootParentPath;
    }

    @ConfigArgSet(required = false, defaultValue = SampleContent.TOUGHDAY_TEMPLATE, desc = "Template for all the pages created.")
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }

    @ConfigArgGet
    public String getTemplate() {
        return this.template;
    }

    @ConfigArgSet(required = false, desc = "How many direct child pages will a page have.",defaultValue = TreePhaser.DEFAULT_BASE)
    public AbstractTest setBase(String base) {
        this.phaser.setBase(Integer.parseInt(base));
        return this;
    }

    @ConfigArgGet
    public int getBase() {
        return this.phaser.getBase();
    }
}
