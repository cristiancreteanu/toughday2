package com.adobe.qe.toughday.tests.sequential.image;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.Tag;
import com.adobe.qe.toughday.api.core.SkippedTestException;
import org.apache.sling.testing.clients.ClientException;
import com.adobe.qe.toughday.tests.sequential.DeletePageTest;

@Tag(tags = { "author" })
@Description(desc = "Test for deleting image assets created by the UploadImageTest")
public class DeleteImageTest extends DeletePageTest {

    public DeleteImageTest() {
    }

    public DeleteImageTest(String parentPath, boolean force, String title){
        super(parentPath, force, title);
    }

    @Override
    protected String getNextTitle() throws SkippedTestException {
        if (UploadImageTest.lastCreated.get() == null) {
            throw new SkippedTestException(new ClientException("No image uploaded(by UploadImageTest). Marking as skipped."));
        }
        return UploadImageTest.lastCreated.get().getName();
    }

    @Override
    public AbstractTest newInstance() {
        return new DeleteImageTest(getParentPath(), getForce(), getTitle());
    }

}
