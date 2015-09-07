package com.day.qa.toughday.tests.serial_tests;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.GraniteConstants;
import com.adobe.granite.testing.client.GraniteClient;
import com.adobe.granite.testing.util.InputStreamBodyWithLength;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.cli.CliArg;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by tuicu on 07/09/15.
 */
public class UploadAssetTest extends TestBase {
    private String fileName;
    private String resourcePath;
    private String mimeType;
    private String parentPath;

    public UploadAssetTest() {
    }

    private UploadAssetTest(String fileName, String resourcePath, String mimeType, String parentPath) {
        this.fileName = fileName;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.parentPath = parentPath;
    }

    @Override
    public void test() throws ClientException {
        MultipartEntity multiPartEntity = new MultipartEntity();
        try {
            // the file
            multiPartEntity.addPart("file", new InputStreamBodyWithLength(resourcePath, mimeType, fileName));
            // add String parameters
            multiPartEntity.addPart(GraniteConstants.PARAMETER_CHARSET, new StringBody(GraniteConstants.CHARSET_UTF8));
            multiPartEntity.addPart("fileName", new StringBody(fileName, Charset.forName(GraniteConstants.CHARSET_UTF8)));
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Could not create Multipart Post!", e);
        }

        GraniteClient client = getDefaultClient();
        RequestExecutor req = client.http().doPost(parentPath + ".createasset.html", multiPartEntity);
        checkStatus(req.getResponse().getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Override
    public AbstractTest newInstance() {
        return new UploadAssetTest(fileName, resourcePath, mimeType, parentPath);
    }

    @CliArg
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @CliArg
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @CliArg
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @CliArg
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}
