package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for uploading assets.
 */
public class UploadImageTest extends SequentialTestBase {

    private String fileName = AuthoringTest.DEFAULT_ASSET_NAME;
    private String resourcePath = AuthoringTest.DEFAULT_RESOURCE_PATH;
    private String mimeType = AuthoringTest.DEFAULT_MIME_TYPE; //TODO do we really need this?
    private String parentPath = CreatePageTest.DEFAULT_PARENT_PATH;

    public static ThreadLocal<File> lastCreated = new ThreadLocal<>();
    public static Random rnd = new Random();
    public static final AtomicInteger nextNumber = new AtomicInteger(0);

    private BufferedImage img;
    private InputStream imageStream;

    public UploadImageTest() {
    }

    private UploadImageTest(String fileName, String resourcePath, String mimeType, String parentPath) {
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.parentPath = parentPath;
        this.fileName = fileName;
    }

    @Before
    private void before() throws ClientException, IOException {
        String nextFileName = fileName + nextNumber.getAndIncrement() + ".png";

        // image processing: read, add noise and save to file
        imageStream = UploadImageTest.getImage(this.resourcePath);
        img = ImageIO.read(imageStream);
        addNoise(img);
        File last = new File(workspace, nextFileName);
        ImageIO.write(img, "png", last);
        lastCreated.set(last);
    }

    @Override
    public void test() throws Exception {
        MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            multiPartEntity.addPart("file", new FileBody(lastCreated.get()));

            multiPartEntity.addPart(Constants.PARAMETER_CHARSET, new StringBody(Constants.CHARSET_UTF8));
            multiPartEntity.addPart("fileName", new StringBody(lastCreated.get().getName(),
                            Charset.forName(Constants.CHARSET_UTF8)));
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Could not create Multipart Post!", e);
        }

        String currentParentPath = StringUtils.stripEnd(getCommunication("parentPath", parentPath), "/");
        getDefaultClient().doPost(currentParentPath  + ".createasset.html", multiPartEntity, HttpStatus.SC_OK);
    }

    @After
    private void after() {
        if (!lastCreated.get().delete()) {
            throw new RuntimeException("Cannot delete file " + lastCreated.get().getName());
        }
    }


    @Override
    public AbstractTest newInstance() {
        return new UploadImageTest(fileName, resourcePath, mimeType, parentPath);
    }


    @ConfigArg(required = false, defaultValue = AuthoringTest.DEFAULT_ASSET_NAME, desc = "The name of the file to be created")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @ConfigArg(required = false, defaultValue = AuthoringTest.DEFAULT_RESOURCE_PATH,
            desc = "The image resource path either in the classpath or the filesystem")
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @ConfigArg(required = false, defaultValue = AuthoringTest.DEFAULT_MIME_TYPE, desc = "The mime type of the uploaded image")
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @ConfigArg(required = false, defaultValue = CreatePageTest.DEFAULT_PARENT_PATH, desc = "The path where the image is uploaded")
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * Get an InputStream of an image, either from the filesystem or from the resources.
     * @param filename
     * @return
     * @throws ClientException if filename is not found either on the filesystem or in the resources
     */
    public static InputStream getImage(String filename) throws ClientException {
        InputStream in;
        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            // try the classpath
            in = UploadImageTest.class.getClassLoader().getResourceAsStream(filename);
            if (null == in) {
                throw new ClientException("Could not find " + filename + " in classpath or in path");
            }
        }
        return in;
    }

    /**
     * Add noise to a {@see BufferedImage}
     * @param img
     */
    public static void addNoise(BufferedImage img) {
        for (int i = 0; i < 200; i++) {
            int x = rnd.nextInt(img.getWidth());
            int y = rnd.nextInt(img.getHeight());
            img.setRGB(x, y, Color.CYAN.getRGB());
        }
    }
}
