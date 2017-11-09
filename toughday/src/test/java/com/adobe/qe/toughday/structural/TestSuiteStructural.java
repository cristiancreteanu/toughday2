package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.metrics.Metric;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Method;

/**
 * Created by tuicu on 13/06/16.
 */
@Category(TestTDConstraints.class)
public class TestSuiteStructural extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        for(Class TDtestClass : ReflectionsContainer.getSubTypesOf(AbstractTest.class)) {
            suite.addTest(new TestConstructor("test", TDtestClass));
            for (Method method : TDtestClass.getDeclaredMethods()) {
                if (method.getAnnotation(ConfigArgSet.class) != null) {
                    suite.addTest(new TestConfigSetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigSetAnnotatedMethod("testArguments", method));
                }
                if (method.getAnnotation(ConfigArgGet.class) != null) {
                    suite.addTest(new TestConfigGetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigGetAnnotatedMethod("testArguments", method));
                }
                if (TestAnnotatedMethod.hasAnnotation(method)) {
                    suite.addTest(new TestAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestAnnotatedMethod("testArguments", method));
                }
            }
        }

        for(Class TDpublisherClass : ReflectionsContainer.getSubTypesOf(Publisher.class)) {
            suite.addTest(new TestConstructor("test", TDpublisherClass));
            for (Method method : TDpublisherClass.getDeclaredMethods()) {
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

        for (Class TDMetricClass : ReflectionsContainer.getSubTypesOf(Metric.class)) {
            suite.addTest(new TestConstructor("test", TDMetricClass));
            for (Method method : TDMetricClass.getDeclaredMethods()) {
                if(method.getAnnotation(ConfigArgSet.class) != null) {
                    suite.addTest(new TestConfigSetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigSetAnnotatedMethod("testArguments", method));
                }
                if(method.getAnnotation(ConfigArgGet.class) != null) {
                    suite.addTest(new TestConfigGetAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigGetAnnotatedMethod("testArguments", method));
                }
            }

        }
        return suite;
    }
}
