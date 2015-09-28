package com.day.qa.toughday.tests.sequential;

import com.day.qa.toughday.core.AbstractTest;

/**
 * Created by tuicu on 14/09/15.
 */
public class DeleteAssetTest extends DeletePageTest {
    public DeleteAssetTest() {
    }

    public DeleteAssetTest(String parentPath, boolean force, String title){
        super(parentPath, force, title);
    }

    @Override
    protected String getNextTitle() {
        return UploadAssetTest.lastCreated.get(Thread.currentThread());
    }

    @Override
    public AbstractTest newInstance() {
        return new DeleteAssetTest(getParentPath(), getForce(), getTitle());
    }

}
