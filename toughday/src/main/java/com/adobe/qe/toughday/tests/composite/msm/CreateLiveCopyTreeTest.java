package com.adobe.qe.toughday.tests.composite.msm;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.tests.sequential.CreatePageTreeTest;
import com.adobe.qe.toughday.tests.sequential.msm.CreateLiveCopyFromPageTest;
import com.adobe.qe.toughday.tests.sequential.msm.RolloutTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;

public class CreateLiveCopyTreeTest  extends CompositeTest {

    private static final String DEFAULT_SOURCE_PAGE_TITLE = "msmsrc";
    private CreatePageTreeTest createPageTest;
    private CreateLiveCopyFromPageTest createLcTest;
    private RolloutTest rolloutTest;

    public CreateLiveCopyTreeTest() { this(true); }

    public CreateLiveCopyTreeTest(boolean createChildren) {
        if (createChildren) {

            createLcTest = new CreateLiveCopyFromPageTest();
            createLcTest.setGlobalArgs(this.getGlobalArgs());

            createPageTest = new CreatePageTreeTest();
            createPageTest.setTitle(DEFAULT_SOURCE_PAGE_TITLE);
            createPageTest.setGlobalArgs(this.getGlobalArgs());
            // set root parent path of create_lc_test to be the same as the source
            createLcTest.setDestinationRoot(createPageTest.rootParentPath);

            rolloutTest = new RolloutTest();
            rolloutTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createPageTest);
            this.addChild(createLcTest);
            this.addChild(rolloutTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateLiveCopyTreeTest(false);
    }

    @ConfigArgSet(required = false, defaultValue = WcmUtils.DEFAULT_TEMPLATE,
            desc="Template for the source pages being created" )
    public CreateLiveCopyTreeTest setPageTemplate(String template) {
        createPageTest.setTemplate(template);
        return this;
    }

    @ConfigArgGet
    public String getPageTemplate() {
        return this.createPageTest.getTemplate();
    }

    @ConfigArgSet(required = false, desc = "The path prefix for all source pages.")
    public CreateLiveCopyTreeTest setParentPath(String parentPath) {
        createPageTest.setParentPath(parentPath);
        createLcTest.setDestinationRoot(parentPath);
        return this;
    }

    @ConfigArgGet
    public String getParentPath() {
        return this.createPageTest.getParentPath();
    }

    @ConfigArgSet(required = false, defaultValue = DEFAULT_SOURCE_PAGE_TITLE,
            desc = "The title of the source page of the LC.")
    public CreateLiveCopyTreeTest setSourcePageTitle(String title) {
        this.createPageTest.setTitle(title);
        return this;
    }

    @ConfigArgGet
    public String getSourcePageTitle() {
        return this.createPageTest.getTitle();
    }

    @ConfigArgSet(required = false, defaultValue = CreateLiveCopyFromPageTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the source page of the LC.")
    public CreateLiveCopyTreeTest setDestinationPageTitle(String title) {
        this.createLcTest.setTitle(title);
        return this;
    }

    @ConfigArgGet
    public String getDestinationPageTitle() {
        return this.createLcTest.getTitle();
    }

    @ConfigArgSet(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public CreateLiveCopyTreeTest setBase(String base) {
        this.createPageTest.setBase(base);
        this.createLcTest.setBase(base);
        return this;
    }

    @ConfigArgGet
    public int getBase() {
        return this.createPageTest.getBase();
    }

    @ConfigArgSet(required = false, desc = "Whether to rollout page / deep", defaultValue = "page")
    public AbstractTest setType(String type) {
        this.rolloutTest.setType(type);
        return this;
    }

    @ConfigArgGet
    public String getType() {
        return this.rolloutTest.getType();
    }

    @ConfigArgSet(required = false, desc = "true/false - Whether to rollout in the background", defaultValue = "false")
    public AbstractTest setBackground(String background) {
        this.rolloutTest.setBackground(background);
        return this;
    }

    @ConfigArgGet
    public boolean getBackground() {
        return this.rolloutTest.getBackground();
    }
}
