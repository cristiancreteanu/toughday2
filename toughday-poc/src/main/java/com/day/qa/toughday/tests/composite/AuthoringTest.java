package com.day.qa.toughday.tests.composite;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.CompositeTest;
import com.day.qa.toughday.core.cli.CliArg;
import com.day.qa.toughday.tests.sequential.CreatePageTest;
import com.day.qa.toughday.tests.sequential.DeleteAssetTest;
import com.day.qa.toughday.tests.sequential.DeletePageTest;
import com.day.qa.toughday.tests.sequential.UploadAssetTest;

/**
 * Created by tuicu on 10/09/15.
 */
public class AuthoringTest extends CompositeTest{
    private CreatePageTest createPageTest;
    private UploadAssetTest uploadAssetTest;
    private DeletePageTest deletePageTest;
    private DeleteAssetTest deleteAssetTest;

    public AuthoringTest() {
        this(true);
    }

    public AuthoringTest(boolean createChildren) {
        if(createChildren) {
            this.createPageTest = new CreatePageTest();
            this.uploadAssetTest = new UploadAssetTest();
            this.deletePageTest = new DeletePageTest();
            this.deleteAssetTest = new DeleteAssetTest();

            this.addChild(createPageTest);
            this.addChild(uploadAssetTest);
            this.addChild(deletePageTest);
            this.addChild(deleteAssetTest);

            this.deletePageTest.setForce(Boolean.toString(true));
            this.deleteAssetTest.setForce(Boolean.toString(true));
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new AuthoringTest(false);
    }

    @CliArg
    public AuthoringTest setPageTemplate(String template) {
        createPageTest.setTemplate(template);
        return this;
    }

    @CliArg
    public AuthoringTest setParentPath(String parentPath) {
        createPageTest.setParentPath(parentPath);
        deletePageTest.setParentPath(parentPath);
        uploadAssetTest.setParentPath(parentPath);
        deleteAssetTest.setParentPath(parentPath);
        return this;
    }

    @CliArg
    public AuthoringTest setResourcePath(String resourcePath) {
        uploadAssetTest.setResourcePath(resourcePath);
        return this;
    }

    @CliArg
    public AuthoringTest setMimeType(String mimeType) {
        uploadAssetTest.setMimeType(mimeType);
        return this;
    }

    @CliArg
    public AuthoringTest setPageTitle(String title) {
        this.createPageTest.setTitle(title);
        this.deletePageTest.setTitle(title);
        return this;
    }

    @CliArg
    public AuthoringTest setAssetName(String name) {
        this.uploadAssetTest.setFileName(name);
        this.deleteAssetTest.setTitle(name);
        return this;
    }
}
