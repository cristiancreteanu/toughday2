package com.adobe.qe.toughday;

import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.internal.core.ReflectionsContainer;
import com.adobe.qe.toughday.internal.core.metrics.Metric;
import com.adobe.qe.toughday.structural.TestConfigGetAnnotatedMethod;
import com.adobe.qe.toughday.structural.TestConfigSetAnnotatedMethod;
import com.adobe.qe.toughday.structural.TestConstructor;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Method;

public class TestInternalSuiteStructural extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        System.out.println("Here!!!");
        for (Class TDMetricClass : ReflectionsContainer.getSubTypesOf(Metric.class)) {
            suite.addTest(new TestConstructor("test", TDMetricClass));
            for (Method method : TDMetricClass.getDeclaredMethods()) {
                if (method.getAnnotation(ConfigArgSet.class) != null) {
                    suite.addTest(new TestConfigSetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigSetAnnotatedMethod("testArguments", method));
                }
                if (method.getAnnotation(ConfigArgGet.class) != null) {
                    suite.addTest(new TestConfigGetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigGetAnnotatedMethod("testArguments", method));
                }
            }

        }

        return suite;
    }
}
