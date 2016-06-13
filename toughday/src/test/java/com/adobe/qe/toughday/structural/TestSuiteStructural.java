package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.ReflectionsContainer;
import com.adobe.qe.toughday.core.config.ConfigArg;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.reflections.Reflections;

import java.lang.reflect.Method;

/**
 * Created by tuicu on 13/06/16.
 */
public class TestSuiteStructural extends TestCase {

    public static TestSuite suite() {
        Reflections reflections = ReflectionsContainer.getReflections();
        TestSuite suite = new TestSuite();

        for(Class TDtestClass : reflections.getSubTypesOf(AbstractTest.class)) {
            suite.addTest(new TestConstructor("test", TDtestClass));
            for (Method method : TDtestClass.getDeclaredMethods()) {
                if(method.getAnnotation(ConfigArg.class) != null) {
                    suite.addTest(new TestConfigAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigAnnotatedMethod("testArguments", method));
                }
                if (TestAnnotatedMethod.hasAnnotation(method)) {
                    suite.addTest(new TestAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestAnnotatedMethod("testArguments", method));
                }
            }
        }

        for(Class TDpublisherClass : reflections.getSubTypesOf(Publisher.class)) {
            suite.addTest(new TestConstructor("test", TDpublisherClass));
            for (Method method : TDpublisherClass.getDeclaredMethods()) {
                if(method.getAnnotation(ConfigArg.class) != null) {
                    suite.addTest(new TestConfigAnnotatedMethod("testModifier", method));
                    suite.addTest(new TestConfigAnnotatedMethod("testArguments", method));
                }
            }
        }
        return suite;
    }
}
