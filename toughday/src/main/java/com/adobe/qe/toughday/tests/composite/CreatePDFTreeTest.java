package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.tests.sequential.CreateFolderTreeTest;
import com.adobe.qe.toughday.tests.sequential.UploadPDFTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;

/**
 * Created by tuicu on 03/11/16.
 */
public class CreatePDFTreeTest extends CompositeTest {
    private CreateFolderTreeTest createFolderTreeTest;
    private UploadPDFTest uploadPDFTest;

    public CreatePDFTreeTest() { this(true); }

    public CreatePDFTreeTest(boolean createChildren) {
        if (createChildren) {
            createFolderTreeTest = new CreateFolderTreeTest();
            try {
                uploadPDFTest = new UploadPDFTest();
            } catch (Exception e) {
                //TODO
            }

            createFolderTreeTest.setGlobalArgs(this.getGlobalArgs());
            uploadPDFTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createFolderTreeTest);
            this.addChild(uploadPDFTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreatePDFTreeTest(false);
    }

    @ConfigArg(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the PDF. Internally, this is incremented")
    public AbstractTest setFolderTitle(String title) {
        createFolderTreeTest.setTitle(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the folders. Internally, this is incremented")
    public AbstractTest setPDFTitle(String title) {
        uploadPDFTest.setFileName(title);
        return this;
    }

    @ConfigArg(required = false, defaultValue = CreateFolderTreeTest.DEFAULT_PARENT_PATH,
            desc = "The path prefix for the PDF tree.")
    public AbstractTest setParentPath(String parentPath) {
        createFolderTreeTest.setParentPath(parentPath);
        return this;
    }

    @ConfigArg(required = false, defaultValue = AuthoringTest.DEFAULT_RESOURCE_PATH,
            desc = "The PDF resource path either in the classpath or the filesystem")
    public void setPDFResourcePath(String resourcePath) {
        uploadPDFTest.setResourcePath(resourcePath);
    }

    @ConfigArg(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public void setBase(String base) {
        createFolderTreeTest.setBase(base);
    }
}
