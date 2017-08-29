package com.adobe.qe.toughday.structural;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by tuicu on 05/01/17.
 */

@Category(TestTDConstraints.class)
public class TestConfigGetAnnotatedMethod extends TestCase {
    private Method TDConfigAnnotatedMethod;

    public TestConfigGetAnnotatedMethod(String testName, Method method) {
        super(testName);
        this.TDConfigAnnotatedMethod = method;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() { return new TestSuite(); }

    public void testModifier() {
        assertTrue("ToughDay2 config get method \"" + TDConfigAnnotatedMethod + "\" must be public",
                (TDConfigAnnotatedMethod.getModifiers() & Modifier.PUBLIC) != 0);
    }

    public void testArguments() {
        assertTrue( "ToughDay2 config get method \"" + TDConfigAnnotatedMethod + "\" must have no parameters",
                TDConfigAnnotatedMethod.getParameterTypes().length == 0);
    }
}
