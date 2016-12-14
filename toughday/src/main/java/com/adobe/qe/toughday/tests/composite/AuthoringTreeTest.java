package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.CreatePageTreeTest;
import com.adobe.qe.toughday.tests.sequential.image.UploadImageTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;
import com.adobe.qe.toughday.tests.utils.WcmUtils;

/**
 * Authoring test. Steps: create page, upload asset, delete page, delete asset.
 */
public class AuthoringTreeTest extends CompositeTest {
    public static final String DEFAULT_PAGE_TITLE = "toughday_tree_title";
    public static final String DEFAULT_ASSET_NAME = "toughday_png_asset";
    public static final String DEFAULT_MIME_TYPE = "image/png";
    public static final String DEFAULT_RESOURCE_PATH = "image.png";

    private CreatePageTreeTest createPageTest;
    private UploadImageTest uploadImageTest;

    public AuthoringTreeTest() {
        this(true);
    }

    public AuthoringTreeTest(boolean createChildren) {
        if (createChildren) {
            this.createPageTest = new CreatePageTreeTest();
            this.createPageTest.setGlobalArgs(this.getGlobalArgs());

            this.uploadImageTest = new UploadImageTest();
            this.uploadImageTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createPageTest);
            this.addChild(uploadImageTest);

            this.setPageTitle(DEFAULT_PAGE_TITLE);
            this.setPageTemplate(WcmUtils.DEFAULT_TEMPLATE);
            this.setParentPath(WcmUtils.DEFAULT_PARENT_PATH);
            this.setImageName(DEFAULT_ASSET_NAME);
            this.setMimeType(DEFAULT_MIME_TYPE);
            this.setResourcePath(DEFAULT_RESOURCE_PATH);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new AuthoringTreeTest(false);
    }

    @ConfigArg(required = false, defaultValue = WcmUtils.DEFAULT_TEMPLATE)
    public AuthoringTreeTest setPageTemplate(String template) {
        createPageTest.setTemplate(template);
        return this;
    }

    @ConfigArg(required = false, defaultValue = WcmUtils.DEFAULT_PARENT_PATH,
            desc = "The path prefix for all pages.")
    public AuthoringTreeTest setParentPath(String parentPath) {
        createPageTest.setParentPath(parentPath);
        uploadImageTest.setParentPath(parentPath);
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_RESOURCE_PATH)
    public AuthoringTreeTest setResourcePath(String resourcePath) {
        uploadImageTest.setResourcePath(resourcePath);
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_MIME_TYPE)
    public AuthoringTreeTest setMimeType(String mimeType) {
        uploadImageTest.setMimeType(mimeType);
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_PAGE_TITLE,
            desc = "The title of the page. Internally, this is incremented")
    public AuthoringTreeTest setPageTitle(String title) {
        this.createPageTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = DEFAULT_ASSET_NAME)
    public AuthoringTreeTest setImageName(String name) {
        this.uploadImageTest.setFileName(name);
        return this;
    }

    @ConfigArg(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public AuthoringTreeTest setBase(String base) {
        this.createPageTest.setBase(base);
        return this;
    }
}
