package com.adobe.qe.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.util.FormEntityBuilder;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.composite.AuthoringTreeTest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
@Description(name="create_pages_tree", desc=
                "This test creates pages hierarchically. Each child on each level has 10 children. " +
                "Each author thread fills in a level in the pages tree, up to 10^level")
public class CreatePageTreeTest extends SequentialTestBase {
    public static final Logger LOG = getLogger(CreatePageTreeTest.class);

    // needed for syncronizing
    static class MyPhaser extends Phaser {
        public Thread mon;

        public MyPhaser() {
            super();
            this.monitor();
        }

        public static final int BASE = 10;

        public final AtomicInteger nextChildPerLevel = new AtomicInteger(0);

        private int maxChildrenPerLevel = BASE;

        protected boolean onAdvance(int phase, int registeredParties) {
            // Increase the level
            // reset counter for level
            this.nextChildPerLevel.set(0);
            maxChildrenPerLevel = maxChildrenPerLevel * BASE;
            // Return false, never terminate phaser.
            if (LOG.isDebugEnabled()) LOG.debug("onAdvance. phase=%d registeredParties=%d tid=%d",
                    phase, registeredParties, Thread.currentThread().getId());
            return false;
        }

        public int getNextNode() {
            int childNumber = this.nextChildPerLevel.getAndIncrement();
            if (childNumber >= maxChildrenPerLevel) {
                if (LOG.isDebugEnabled()) LOG.debug("Waiting for sync. tid = " + Thread.currentThread().getId());
                this.arriveAndAwaitAdvance();
                return getNextNode();
            }
            return childNumber;
        }

        public int getLevel() {
            return this.getPhase() + 1;
        }

        public void monitor() {
            this.mon = new Thread() {
                @Override
                public void run() {
                    do {
                        if (nextChildPerLevel.get() >= maxChildrenPerLevel ) {
                            arriveAndAwaitAdvance();
                        } else {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    } while (true);
                }
            };
            this.register();
            mon.start();
        }

    }
    public static final MyPhaser phaser = new MyPhaser();
    static {
        AbstractTest.addExtraThread(phaser.mon);
    }

    private String rootParentPath = DEFAULT_PARENT_PATH;
    private String template = DEFAULT_TEMPLATE;
    private String title = AuthoringTreeTest.DEFAULT_PAGE_TITLE;

    private ThreadLocal<Integer> nextChild = new ThreadLocal<>();

    private ThreadLocal<String> parentPath = new ThreadLocal<>();
    private ThreadLocal<String> nodeName = new ThreadLocal<>();
    private ThreadLocal<Boolean> failed = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public CreatePageTreeTest() {
    }

    protected CreatePageTreeTest(String parentPath, String template, String title) {
        this.rootParentPath = parentPath;
        this.template = template;
        this.title = title;
    }

    public static final String CMD_CREATE_PAGE = "createPage";
    public static final String DEFAULT_PARENT_PATH = "/content/geometrixx/en";
    public static final String DEFAULT_TEMPLATE = "/apps/geometrixx/templates/contentpage";

    @Before
    public void setup() {
        phaser.register();

        // this gets the next node on the level and potentially waits for other threads to reset the level
        // save those values for later use
        this.nextChild.set(phaser.getNextNode());
        this.parentPath.set(rootParentPath + computeParentPath(nextChild.get(), phaser.getLevel()));
        this.nodeName.set(computeNodeName(nextChild.get()));
        this.failed.set(Boolean.FALSE);

    }

    @Override
    public void test() throws ClientException {
        try {
            createPage();
        } catch (Exception e) {
            this.failed.set(Boolean.TRUE);
            // log and throw. It's normally an anti-pattern, but we don't log exceptions anywhere on the upper level,
            // we're just count them.
            LOG.warn("Failed to create page {}{} ({})", parentPath.get(), nodeName.get(), e.getMessage());
            throw e;
        }
        if (LOG.isDebugEnabled()) LOG.debug("tid=%{} nextChild={} level={} path={}",
                Thread.currentThread().getId(), nextChild.get(), phaser.getLevel(), parentPath.get() + nodeName.get());
    }

    @After
    public void after() {
        if (LOG.isDebugEnabled()) LOG.debug("In after() tid={}", Thread.currentThread().getId());
        // make sure the page was created
        for (int i=0; i<5; i++) {
            try {
                // If operation was marked as failed and the path really does not exist,
                // try and create it, as it is needed as the parent path for the children on the next level
                if (!failed.get().booleanValue() || getDefaultClient().exists(this.parentPath.get() + nodeName.get())) {
                    break;
                } else {
                    if (LOG.isDebugEnabled()) LOG.debug("Retrying to create page tid={} nextChild={} phase={} path={}\n",
                            Thread.currentThread().getId(), nextChild.get(), phaser.getLevel(),
                            parentPath.get() + nodeName.get());
                    createPage();
                }
            } catch (ClientException e) {
                LOG.warn("In after(): Failed to create page {}{}", parentPath.get(), nodeName.get());
            }
        }

        // de-register
        phaser.arriveAndDeregister();
    }

    private void createPage() throws ClientException {
        FormEntityBuilder feb = new FormEntityBuilder()
                .addParameter("cmd", CMD_CREATE_PAGE)
                .addParameter("parentPath", parentPath.get())
                .addParameter("title", nodeName.get())
                .addParameter("template", template);

        RequestExecutor req = getDefaultClient().http().doPost("/bin/wcmcommand", feb.getEntity());
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }


    @Override
    public AbstractTest newInstance() {
        return new CreatePageTreeTest(rootParentPath, template, title);
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

    @ConfigArg(required = false, defaultValue = DEFAULT_TEMPLATE)
    public AbstractTest setTemplate(String template) {
        this.template = template;
        return this;
    }


    // private methods

    private static String computeParentPath(int nextChild, int level) {
        if (level == 1) {
            return "/";
        }
        String path = Integer.toString(nextChild / MyPhaser.BASE, MyPhaser.BASE);
        path = StringUtils.leftPad(path, level-1, "0");
        return path.replace("", "/");
    }

    private static String computeNodeName(int nextChild) {
        return Integer.toString(nextChild % MyPhaser.BASE, MyPhaser.BASE);
    }
}
