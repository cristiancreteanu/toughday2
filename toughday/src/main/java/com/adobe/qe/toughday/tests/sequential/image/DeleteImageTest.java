package com.adobe.qe.toughday.tests.sequential.image;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.tests.sequential.DeletePageTest;

@Description(desc = "Test for deleting image assets created by the UploadImageTest")
public class DeleteImageTest extends DeletePageTest {

    public DeleteImageTest() {
    }

    public DeleteImageTest(String parentPath, boolean force, String title){
        super(parentPath, force, title);
    }

    @Override
    protected String getNextTitle() {
        return UploadImageTest.lastCreated.get().getName();
    }

    @Override
    public AbstractTest newInstance() {
        return new DeleteImageTest(getParentPath(), getForce(), getTitle());
    }

}
