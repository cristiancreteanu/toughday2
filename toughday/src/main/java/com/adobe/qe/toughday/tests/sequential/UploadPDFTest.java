package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.config.ConfigArg;
import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.Constants;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Test for uploading PDF assets.
 */
public class UploadPDFTest extends SequentialTestBase {

    public static final String DEFAULT_PARENT_PATH = "/content/dam";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String DEFAULT_PDF_NAME = "toughday_pdf_asset";
    private static final String DEFAULT_PDF_PATH = "document.pdf";

    private String fileName = DEFAULT_PDF_NAME;
    private String resourcePath = DEFAULT_PDF_PATH;
    private String parentPath = DEFAULT_PARENT_PATH;

    private String currentID;
    private File currentFile;

    public UploadPDFTest() throws IOException {
        //Force font caching
        PDDocument doc = new PDDocument();
        PDPageContentStream pdStream = new PDPageContentStream(doc, new PDPage(), PDPageContentStream.AppendMode.APPEND, true, true);
        pdStream.setFont(PDType1Font.HELVETICA_BOLD, 10.0f);
        pdStream.close();
        doc.close();
    }

    private UploadPDFTest(String fileName, String resourcePath, String parentPath) {
        this.resourcePath = resourcePath;
        this.parentPath = parentPath;
        this.fileName = fileName;
    }

    @Before
    private void before() throws ClientException, IOException {
        currentID = UUID.randomUUID().toString();
        String nextFileName = fileName + currentID + ".pdf";

        // image processing: read, add noise and save to file
        PDDocument doc = PDDocument.load( getPDF(this.resourcePath));
        addNoise(doc, currentID);
        currentFile = new File(workspace, nextFileName);
        doc.save(currentFile);
        doc.close();
    }

    @Override
    public void test() throws Exception {
        MultipartEntityBuilder multiPartEntity = MultipartEntityBuilder.create();
        try {
            multiPartEntity.addPart("file", new FileBody(currentFile));
            multiPartEntity.addPart(Constants.PARAMETER_CHARSET, new StringBody(Constants.CHARSET_UTF8));
            multiPartEntity.addPart("fileName", new StringBody(currentFile.getName(),
                    Charset.forName(Constants.CHARSET_UTF8)));
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Could not create Multipart Post!", e);
        }

        getDefaultClient().doPost(parentPath + ".createasset.html", multiPartEntity.build(), HttpStatus.SC_OK);
    }

    @After
    private void after() {
        currentFile.delete();
    }

    @Override
    public AbstractTest newInstance() {
        return new UploadPDFTest(fileName, resourcePath, parentPath);
    }

    @ConfigArg(required = false, defaultValue = UploadPDFTest.DEFAULT_PDF_NAME, desc = "The name of the file to be created")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @ConfigArg(required = false, defaultValue = UploadPDFTest.DEFAULT_PDF_PATH,
            desc = "The image resource path either in the classpath or the filesystem")
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @ConfigArg(required = false, defaultValue = UploadPDFTest.DEFAULT_PARENT_PATH, desc = "The path where the image is uploaded")
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * Get an InputStream of a PDF, either from the filesystem or from the resources.
     * @param filename
     * @return
     * @throws ClientException if filename is not found either on the filesystem or in the resources
     */
    public static InputStream getPDF(String filename) throws ClientException {
        InputStream in;
        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            // try the classpath
            in = UploadPDFTest.class.getClassLoader().getResourceAsStream(filename);
            if (null == in) {
                throw new ClientException("Could not find " + filename + " in classpath or in path");
            }
        }
        return in;
    }

    /**
     * Add noise to PDF
     * @param doc
     */
    private static void addNoise(PDDocument doc, String ID) throws IOException {
        for(PDPage page : doc.getPages()) {
            //add an ID to each page
            PDPageContentStream pdStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            pdStream.setFont(PDType1Font.HELVETICA_BOLD, 10.0f);
            pdStream.beginText();
            pdStream.showText(ID);
            pdStream.endText();
            pdStream.close();
        }
    }
}
