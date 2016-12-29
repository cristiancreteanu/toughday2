package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Name;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.*;
import com.adobe.qe.toughday.tests.sequential.tags.AddTagToResourceTest;
import com.adobe.qe.toughday.tests.sequential.tags.CreateTagTreeTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;

/**
 * Created by tuicu on 05/11/16.
 */
@Name(name="CreateTaggedPagesTree")
@Description(desc=
        "This test creates tags and pages hierarchically. Each page gets assigned two tags. " +
                "One from the corresponding node in the tag tree and one that is the same for the whole page tree. " +
                "Each child on each level has 10 children. " +
                "Each author thread fills in a level in the tag tree, up to 10^level")
public class CreateTaggedPagesTree extends CompositeTest {

    private CreateTagTreeTest createTagTreeTest;
    private CreatePageTreeTest createPageTreeTest;
    private AddTagToResourceTest addTagToResourceTest;

    public CreateTaggedPagesTree() { this(true); }

    public CreateTaggedPagesTree(boolean createChildren) {
        if (createChildren) {
            createTagTreeTest = new CreateTagTreeTest();
            createPageTreeTest = new CreatePageTreeTest();
            addTagToResourceTest = new AddTagToResourceTest();

            createTagTreeTest.setGlobalArgs(this.getGlobalArgs());
            createPageTreeTest.setGlobalArgs(this.getGlobalArgs());
            addTagToResourceTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createTagTreeTest);
            this.addChild(createPageTreeTest);
            this.addChild(addTagToResourceTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateTaggedPagesTree(false);
    }

    @ConfigArg(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AbstractTest setPageTitle(String title) {
        createPageTreeTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = WcmUtils.DEFAULT_PARENT_PATH,
            desc = "The path prefix for all pages.")
    public AbstractTest setParentPath(String parentPath) {
        createPageTreeTest.setParentPath(parentPath);
        return this;
    }

    @ConfigArg(required = false, defaultValue = WcmUtils.DEFAULT_TEMPLATE,
        desc = "The title of the pages. Internally, this will be incremented")
    public AbstractTest setTemplate(String template) {
        createPageTreeTest.setTemplate(template);
        return this;
    }

    @ConfigArg(required = false, defaultValue = CreateTagTreeTest.DEFAULT_NAMESPACE,
            desc = "The title of the tags. Internally, this will be incremented")
    public AbstractTest setTagTitle(String title) {
        createTagTreeTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public AbstractTest setBase(String base) {
        createPageTreeTest.setBase(base);
        createTagTreeTest.setBase(base);
        return this;
    }
}
