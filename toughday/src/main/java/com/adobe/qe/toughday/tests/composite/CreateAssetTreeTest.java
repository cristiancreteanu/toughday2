package com.adobe.qe.toughday.tests.composite;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.tests.sequential.CreateFolderTreeTest;
import com.adobe.qe.toughday.tests.sequential.image.UploadImageTest;
import com.adobe.qe.toughday.tests.utils.TreePhaser;

/**
 * Created by tuicu on 02/11/16.
 */
public class CreateAssetTreeTest extends CompositeTest {

    private CreateFolderTreeTest createFolderTreeTest;
    private UploadImageTest uploadImageTest;

    public CreateAssetTreeTest() { this(true); }

    public CreateAssetTreeTest(boolean createChildren) {
        if (createChildren) {
            createFolderTreeTest = new CreateFolderTreeTest();
            uploadImageTest = new UploadImageTest();

            createFolderTreeTest.setGlobalArgs(this.getGlobalArgs());
            uploadImageTest.setGlobalArgs(this.getGlobalArgs());

            this.addChild(createFolderTreeTest);
            this.addChild(uploadImageTest);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateAssetTreeTest(false);
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the assets. Internally, this is incremented")
    public AbstractTest setFolderTitle(String title) {
        createFolderTreeTest.setTitle(title);
        return this;
    }

    @ConfigArgGet
    public String getFolderTitle() {
        return createFolderTreeTest.getTitle();
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTreeTest.DEFAULT_PAGE_TITLE,
            desc = "The title of the folders. Internally, this is incremented")
    public AbstractTest setAssetTitle(String title) {
        uploadImageTest.setFileName(title);
        return this;
    }

    @ConfigArgGet
    public String getAssetTitle() {
        return this.uploadImageTest.getFileName();
    }

    @ConfigArgSet(required = false, defaultValue = CreateFolderTreeTest.DEFAULT_PARENT_PATH,
            desc = "The path prefix for the asset tree.")
    public AbstractTest setParentPath(String parentPath) {
        createFolderTreeTest.setParentPath(parentPath);
        return this;
    }

    @ConfigArgGet
    public String getParentPath() {
        return this.createFolderTreeTest.getParentPath();
    }

    @ConfigArgSet(required = false, defaultValue = AuthoringTest.DEFAULT_RESOURCE_PATH,
            desc = "The image resource path either in the classpath or the filesystem")
    public void setAssetResourcePath(String resourcePath) {
        uploadImageTest.setResourcePath(resourcePath);
    }

    @ConfigArgGet
    public String getAssetResourcePath() {
        return this.getAssetResourcePath();
    }

    @ConfigArgSet(required = false, defaultValue = TreePhaser.DEFAULT_BASE)
    public AbstractTest setBase(String base) {
        createFolderTreeTest.setBase(base);
        return this;
    }

    @ConfigArgGet
    public int getBase() {
        return this.createFolderTreeTest.getBase();
    }
}
