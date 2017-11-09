package com.adobe.qe.toughday.internal.core.config;

import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import com.adobe.qe.toughday.tests.composite.AuthoringTreeTest;
import com.adobe.qe.toughday.tests.composite.CreateAssetTreeTest;
import com.adobe.qe.toughday.tests.composite.msm.CreateLiveCopyTreeTest;
import com.adobe.qe.toughday.tests.sequential.GetHomepageTest;
import com.adobe.qe.toughday.tests.sequential.GetTest;
import com.adobe.qe.toughday.tests.sequential.image.DeleteImageTest;
import com.adobe.qe.toughday.tests.sequential.search.QueryBuilderTest;

import java.util.HashMap;

/**
 * Class for holding predefined suites. Don't turn this into Singleton, or make a static map, otherwise two Configuration
 * objects will change the same suite and it will result in unexpected behaviour.
 */
public class PredefinedSuites extends HashMap<String, TestSuite> {
    public static final String DEFAULT_SUITE_NAME = "toughday";

    public PredefinedSuites() {
        put("get_tests", new TestSuite()
                        .add(new GetHomepageTest().setName("Get Homepage"), 10)
                        .add(new GetTest().setPath("/sites.html").setName("Get /sites.html"), 5)
                        //.add(new GetTest().setPath("/projects.html").setName("Get /projects.html"), 5)
                        .add(new GetTest().setPath("/assets.html").setName("Get /assets.html"), 5)
                        // maybe more here?
                        .setDescription("Executes GET requests on common paths")
                        .addTag("author")
        );
        put("tree_authoring", new TestSuite()
                        .add(new AuthoringTreeTest().setName("Tree Authoring"), 2)
                        .setDescription("A full authoring test with \"create hierarchical pages\", \"upload asset\", " +
                                "\"delete asset\". The pages are not deleted.")
                        .addTag("author")
        );
        put("authoring", new TestSuite()
                        .add(new AuthoringTest().setName("Authoring"), 2)
                        .setDescription("A full authoring test with \"create page\", \"upload asset\", " +
                                "\"delete asset\", \"delete page\" steps. " +
                                "The pages are deleted.")
                        .addTag("author")
        );
        put("publish", new TestSuite()
                        .add(new GetHomepageTest(), 1)
                        .add(new GetTest().setPath("/").setName("Get1"), 1)
                        .add(new GetTest().setPath("/").setName("Get2"), 1)
                        .addTag("publish")
                        .setDescription("Publish suite")
        );
        put("toughday", new TestSuite()
                        .add(new CreateLiveCopyTreeTest()
                                .setBase(String.valueOf(5))
                                .setSourcePageTitle("IAmAPage")
                                .setName("CreateLiveCopy"), 5, -1, 80000)
                        .add(new CreateAssetTreeTest()
                                .setAssetTitle("IAmAnAsset")
                                .setFolderTitle("IAmAFolder")
                                .setBase(String.valueOf(3))
                                .setName("UploadAsset"), 5, -1, 20000)
                        .add(new DeleteImageTest()
                                .setName("DeleteAsset"),  5, -1, 20000)
                        .add(new QueryBuilderTest()
                                .setQuery("type=nt:unstructured&group.1_path=/libs&orderby=@jcr:score&orderby.sort=desc")
                                .setName("Query"), 10)
                        .add(new GetHomepageTest()
                                .setName("GetHomepage"), 75)
                        .setDescription("A heavy duty suite of AEM use cases. " +
                                "It performs operations like: search, upload assets, delete assets, create pages, live copies and folders and gets the home page. " +
                                "It has a proportion of 15% writes vs 85% reads.")
                        .addTag("author")
        );
    }

    public TestSuite getDefaultSuite() {
        return this.get(DEFAULT_SUITE_NAME);
    }
}
