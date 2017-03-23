package com.adobe.qe.toughday.tests.sequential.msm;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.*;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.samplecontent.SampleContent;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;
import org.apache.logging.log4j.Logger;

import java.util.UUID;


@Name(name = "create_lc")
@Description(desc = "Creates live copies from pages")
public class CreateLiveCopyFromPageTest extends SequentialTestBase {
    public static final Logger LOG = createLogger(CreateLiveCopyFromPageTest.class);

    public static final String SOURCE_PAGE_NAME = "create_lc_source";
    public static final String DESTINATION_PAGE_NAME = "create_lc_dest";
    public static  final String DEFAULT_SOURCE_ROOT_PAGE = "/content/toughday/language-master/en/toughday";


    public static final String DEFAULT_DESTINATION_ROOT_PAGE = SampleContent.TOUGHDAY_SITE;
    public static final String DEFAULT_PAGE_TITLE = "lc";

    private final TreePhaser phaser;

    private String title;
    private String sourcePage;
    private String destinationPage;
    private String destinationRoot;
    private String nodeName;
    private boolean failed = false;
    private int nextChild;


    public CreateLiveCopyFromPageTest() {
        this.phaser = new TreePhaser();
        this.sourcePage = DEFAULT_SOURCE_ROOT_PAGE;
        this.destinationRoot = DEFAULT_DESTINATION_ROOT_PAGE;
        this.title = DEFAULT_PAGE_TITLE;
    }

    public CreateLiveCopyFromPageTest(TreePhaser phaser, String title, String sourcePage, String destinationRoot) {
        this.phaser = phaser;
        this.sourcePage = sourcePage;
        this.destinationRoot = destinationRoot;
        this.title = title;
    }

    @Setup
    private void setup() throws Exception {
        String isolatedFolder = "toughday_lc" + UUID.randomUUID();
        try {
            getDefaultClient().createFolder(isolatedFolder, isolatedFolder, destinationRoot);
            destinationRoot = destinationRoot + "/" + isolatedFolder;
        } catch (Exception e) {
            LOG.debug("Could not create isolated folder for running " + getFullName());
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

    @ConfigArgSet(required = false, desc = "The source page for live copies", defaultValue = DEFAULT_SOURCE_ROOT_PAGE)
    public AbstractTest setSourcePage(String page) {
        this.sourcePage = page;
        return this;
    }

    @ConfigArgGet
    public String getSourcePage() {
        return this.sourcePage;
    }

    @ConfigArgSet(required = false, desc = "Default root for live copies", defaultValue = DEFAULT_DESTINATION_ROOT_PAGE)
    public AbstractTest setDestinationRoot(String page) {
        this.destinationRoot = page;
        return this;
    }

    @ConfigArgGet
    public String getDestinationRoot() {
        return this.destinationRoot;
    }

    @ConfigArgSet(required = false, desc = "Title for livecopies", defaultValue = DEFAULT_PAGE_TITLE)
    public AbstractTest setTitle(String title) {
        this.title = title;
        return this;
    }

    @ConfigArgGet
    public String getTitle() {
        return this.title;
    }

    @ConfigArgSet(required = false, desc = "How many direct child pages will a page have.", defaultValue = TreePhaser.DEFAULT_BASE)
    public AbstractTest setBase(String base) {
        this.phaser.setBase(Integer.parseInt(base));
        return this;
    }

    @ConfigArgGet
    public int getBase() {
        return this.phaser.getBase();
    }
}
