package com.adobe.qe.toughday.tests.sequential.msm;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.*;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;
import org.apache.logging.log4j.Logger;


@Name(name = "create_lc")
@Description(desc = "Creates live copies from pages")
public class CreateLiveCopyFromPageTest extends SequentialTestBase {
    public static final Logger LOG = createLogger(CreateLiveCopyFromPageTest.class);

    public static final String SOURCE_PAGE_NAME = "create_lc_source";
    public static final String DESTINATION_PAGE_NAME = "create_lc_dest";
    public final String defaultSourceRootPage = rootNodePath + "/" + SOURCE_PAGE_NAME;


    public final String defaultDestinationRootPage = rootNodePath + "/" + DESTINATION_PAGE_NAME;
    public static final String DEFAULT_PAGE_TITLE = "lc";

    private final TreePhaser phaser;
    private String template = WcmUtils.DEFAULT_TEMPLATE;

    private String title;
    private String sourcePage;
    private String destinationPage;
    private String destinationRoot;
    private String nodeName;
    private boolean failed = false;
    private int nextChild;


    public CreateLiveCopyFromPageTest() {
        this.phaser = new TreePhaser();
        this.sourcePage = defaultSourceRootPage;
        this.destinationRoot = defaultDestinationRootPage;
        this.title = DEFAULT_PAGE_TITLE;
    }

    public CreateLiveCopyFromPageTest(TreePhaser phaser, String title, String sourcePage, String destinationRoot) {
        this.phaser = phaser;
        this.sourcePage = sourcePage;
        this.destinationRoot = destinationRoot;
        this.title = title;
    }

    @FactorySetup
    private void setup() throws Exception {
        this.prepareContent();
        if (!getDefaultClient().exists(defaultDestinationRootPage)) {
            WcmUtils.createPage(getDefaultClient(), rootNodePath, DESTINATION_PAGE_NAME, template, 200, 201);
        }
        if (!getDefaultClient().exists(defaultSourceRootPage)) {
            WcmUtils.createPage(getDefaultClient(), rootNodePath, SOURCE_PAGE_NAME, template, 200, 201);
        }
    }

    @Before
    private void before() throws Exception {
        this.sourcePage = getCommunication("resource", sourcePage);
        phaser.register();

        // this gets the next node on the level and potentially waits for other threads to reset the level
        // save those values for later use
        this.nextChild = phaser.getNextNode();
        this.destinationPage = TreePhaser.computeParentPath(nextChild, phaser.getLevel(),
                phaser.getBase(), title, destinationRoot);
        this.nodeName = TreePhaser.computeNodeName(nextChild, phaser.getBase(), title);
        this.failed = false;
    }

    @Override
    public void test() throws Exception {
        try {
            createLC();
        } catch (Exception e) {
            this.failed = true;
            // log and throw. It's normally an anti-pattern, but we don't log exceptions anywhere on the upper level,
            // we're just count them.
            LOG.warn("Failed to create page {} ({})", destinationPage, e.getMessage());
            throw e;
        }
    }

    private void createLC() throws Exception {
        WcmUtils.createLiveCopy(getDefaultClient(), nodeName, title, destinationPage, sourcePage, false, null, null, false, 200);
        communicate("livecopy", destinationPage + nodeName);
    }

    @After
    private void after() {
        if (LOG.isDebugEnabled()) LOG.debug("In after() tid={}", Thread.currentThread().getId());
        // make sure the page was created
        for (int i=0; i<5; i++) {
            try {
                // If operation was marked as failed and the path really does not exist,
                // try and create it, as it is needed as the parent path for the children on the next level
                if (!failed || getDefaultClient().exists(this.destinationPage + nodeName)) {
                    break;
                } else {
                    if (LOG.isDebugEnabled()) LOG.debug("Retrying to create LC tid={} nextChild={} phase={} path={}\n",
                            Thread.currentThread().getId(), nextChild, phaser.getLevel(),
                            destinationPage + nodeName);
                    createLC();
                }
            } catch (Exception e) {
                LOG.warn("In after(): Failed to create LC {}{}", destinationPage, nodeName);
            }
        }

        // de-register
        phaser.arriveAndDeregister();
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateLiveCopyFromPageTest(phaser, title, sourcePage, destinationRoot);
    }

    @ConfigArg(required = false, desc = "The source page for live copies")
    public AbstractTest setSourcePage(String page) {
        this.sourcePage = page;
        return this;
    }

    @ConfigArg(required = false, desc = "Default root for live copies")
    public AbstractTest setDestinationRoot(String page) {
        this.destinationRoot = page;
        return this;
    }

    @ConfigArg(required = false, desc = "Title for livecopies")
    public AbstractTest setTitle(String title) {
        this.title = title;
        return this;
    }

    @ConfigArg(required = false, desc = "How many direct child pages will a page have.", defaultValue = TreePhaser.DEFAULT_BASE)
    public AbstractTest setBase(String base) {
        this.phaser.setBase(Integer.parseInt(base));
        return this;
    }
}
