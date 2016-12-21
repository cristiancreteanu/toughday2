package com.adobe.qe.toughday.tests.composite.msm;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.CreatePageTreeTest;
import com.adobe.qe.toughday.tests.sequential.msm.CreateLiveCopyFromPageTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;

public class CreateLiveCopyTreeTest  extends CompositeTest {

    private static final String DEFAULT_SOURCE_PAGE_TITLE = "msmsrc";
    private CreatePageTreeTest createPageTest;
    private CreateLiveCopyFromPageTest createLcTest;

    public CreateLiveCopyTreeTest() { this(true); }

    public CreateLiveCopyTreeTest(boolean createChildren) {
        if (createChildren) {
            createLcTest = new CreateLiveCopyFromPageTest();
            createLcTest.setGlobalArgs(this.getGlobalArgs());

            createPageTest = new CreatePageTreeTest();
            createPageTest.setTitle(DEFAULT_SOURCE_PAGE_TITLE);
            createPageTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createPageTest);
            this.addChild(createLcTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateLiveCopyTreeTest(false);
    }

    @ConfigArg(required = false, defaultValue = WcmUtils.DEFAULT_TEMPLATE,
            desc="Template for the source pages being created" )
    public CreateLiveCopyTreeTest setPageTemplate(String template) {
        createPageTest.setTemplate(template);
        return this;
    }

    @ConfigArg(required = false, desc = "The path prefix for all source pages.")
    public CreateLiveCopyTreeTest setParentPath(String parentPath) {
        createPageTest.setParentPath(parentPath);
        createLcTest.setDestinationRoot(parentPath);
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_SOURCE_PAGE_TITLE,
            desc = "The title of the source page of the LC.")
    public CreateLiveCopyTreeTest setSourcePageTitle(String title) {
        this.createPageTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = CreateLiveCopyFromPageTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the source page of the LC.")
    public CreateLiveCopyTreeTest setDestinationPageTitle(String title) {
        this.createLcTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public CreateLiveCopyTreeTest setBase(String base) {
        this.createPageTest.setBase(base);
        this.createLcTest.setBase(base);
        return this;
    }
}
