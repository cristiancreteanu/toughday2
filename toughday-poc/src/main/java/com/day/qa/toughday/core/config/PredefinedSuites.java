package com.day.qa.toughday.core.config;

import com.day.qa.toughday.core.TestSuite;
import com.day.qa.toughday.tests.sequential.GetHomepageTest;

import java.util.HashMap;

/**
 * Created by tuicu on 28/09/15.
 * Class for holding predefined suites. Don't turn this into Singleton, or make a static map, otherwise two Configuration
 * objects will change the same suite and it will result in unexpected behaviour.
 */
public class PredefinedSuites extends HashMap<String, TestSuite> {
    public PredefinedSuites() {
        put("DummySuite", new TestSuite()
                .add(new GetHomepageTest().setName("DummyTest"), 10).setDescription("An awesome test suite"));
    }
}
