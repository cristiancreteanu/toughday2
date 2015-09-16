package com.day.qa.toughday.tests.sequential;

import com.adobe.granite.testing.ClientException;
import com.adobe.granite.testing.GraniteConstants;
import com.adobe.granite.testing.client.GraniteClient;
import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.cli.CliArg;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.http.RequestExecutor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 07/09/15.
 */
public class UploadAssetTest extends SequentialTestBase {

    private String fileName;
    private String resourcePath;
    private String mimeType;
    private String parentPath;

    public static ConcurrentHashMap<Thread, String> lastCreated = new ConcurrentHashMap<Thread, String>();
    public static final AtomicInteger nextNumber = new AtomicInteger(0);

    public UploadAssetTest() {
    }

    private UploadAssetTest(String fileName, String resourcePath, String mimeType, String parentPath) {
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.parentPath = parentPath;
        this.fileName = fileName;
    }

    @Override
    public void test() throws ClientException {
        String nextFileName = fileName + nextNumber.getAndIncrement();
        lastCreated.put(Thread.currentThread(), nextFileName);

        MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            // the file
            //multiPartEntity.addPart("file", new InputStreamBodyWithLength(resourcePath, mimeType, fileName));
            multiPartEntity.addPart("file", new FileBody(new File(resourcePath)));
            // add String parameters

            multiPartEntity.addPart(GraniteConstants.PARAMETER_CHARSET, new StringBody(GraniteConstants.CHARSET_UTF8));
            multiPartEntity.addPart("fileName", new StringBody(nextFileName,
                            Charset.forName(GraniteConstants.CHARSET_UTF8))
            );
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
