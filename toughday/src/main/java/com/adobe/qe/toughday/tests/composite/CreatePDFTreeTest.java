package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.annotations.Tag;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.tests.sequential.CreateFolderTreeTest;
import com.adobe.qe.toughday.tests.sequential.UploadPDFTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;

@Tag(tags = { "author" })
@Description(desc="This test creates folders and PDFs hierarchically. " +
        "Each child on each level has \"base\" folder children and \"base\" asset children")
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

    @ConfigArgSet(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the folders. Internally, this is incremented")
    public AbstractTest setFolderTitle(String title) {
        createFolderTreeTest.setTitle(title);
        return this;
    }

    @ConfigArgGet
    public String getFolderTitle() {
        return createFolderTreeTest.getTitle();
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the PDF. Internally, this is incremented")
    public AbstractTest setPDFTitle(String title) {
        uploadPDFTest.setFileName(title);
        return this;
    }

    @ConfigArgGet
    public String getPDFTitle() {
        return uploadPDFTest.getFileName();
    }

    @ConfigArgSet(required = false, defaultValue = CreateFolderTreeTest.DEFAULT_PARENT_PATH,
            desc = "The path prefix for the PDF tree.")
    public AbstractTest setParentPath(String parentPath) {
        createFolderTreeTest.setParentPath(parentPath);
        return this;
    }

    @ConfigArgGet
    public String getParentPath() {
        return this.createFolderTreeTest.getParentPath();
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTest.DEFAULT_RESOURCE_PATH,
            desc = "The PDF resource path either in the classpath or the filesystem")
    public void setPDFResourcePath(String resourcePath) {
        uploadPDFTest.setResourcePath(resourcePath);
    }

    @ConfigArgGet
    public String getPDFResourcePath() {
        return uploadPDFTest.getResourcePath();
    }

    @ConfigArgSet(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public void setBase(String base) {
        createFolderTreeTest.setBase(base);
    }

    @ConfigArgGet
    public int getBase() {
        return this.createFolderTreeTest.getBase();
    }
}
