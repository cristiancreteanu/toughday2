package com.adobe.qe.toughday.core.config;

import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.tests.composite.AuthoringTest;
import com.adobe.qe.toughday.tests.sequential.GetHomepageTest;
import com.adobe.qe.toughday.tests.sequential.GetTest;

import java.util.HashMap;

/**
 * Class for holding predefined suites. Don't turn this into Singleton, or make a static map, otherwise two Configuration
 * objects will change the same suite and it will result in unexpected behaviour.
 */
public class PredefinedSuites extends HashMap<String, TestSuite> {
    public static final String DEFAULT_SUITE_NAME = "smoke_tests";

    public PredefinedSuites() {
        put("smoke_tests", new TestSuite()
                        .add(new GetHomepageTest().setName("Get Homepage"), 10)
                        .add(new GetTest().setPath("/sites.html").setName("Get /sites.html"), 5)
                        .add(new GetTest().setPath("/projects.html").setName("Get /projects.html"), 5)
                        .add(new GetTest().setPath("/assets.html").setName("Get /assets.html"), 5)
                        .add(new GetTest().setPath("/screens.html").setName("Get /screens.html"), 5)
                        // maybe more here?
                        .setDescription("Executes GET requests on common paths")
        );
        put("authoring_tests", new TestSuite()
                        .add(new AuthoringTest().setName("Authoring Test"), 2)
                        .setDescription("A full authoring test with \"create page\", \"upload asset\", " +
                                "\"delete asset\", \"delete page\" steps")
        );
    }

    public TestSuite getDefaultSuite() {
        return this.get(DEFAULT_SUITE_NAME);
    }
}
