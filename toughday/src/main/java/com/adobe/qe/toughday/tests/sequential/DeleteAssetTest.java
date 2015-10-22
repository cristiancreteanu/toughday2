package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;

/**
 *
 */
public class DeleteAssetTest extends DeletePageTest {

    public DeleteAssetTest() {
    }

    public DeleteAssetTest(String parentPath, boolean force, String title){
        super(parentPath, force, title);
    }

    @Override
    protected String getNextTitle() {
        return UploadAssetTest.lastCreated.get().getName();
    }

    @Override
    public AbstractTest newInstance() {
        return new DeleteAssetTest(getParentPath(), getForce(), getTitle());
    }

}
