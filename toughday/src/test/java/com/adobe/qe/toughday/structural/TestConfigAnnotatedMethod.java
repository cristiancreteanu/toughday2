package com.adobe.qe.toughday.structural;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by tuicu on 13/06/16.
 */
public class TestConfigAnnotatedMethod extends TestCase {
    private Method TDConfigAnnotatedMethod;

    public TestConfigAnnotatedMethod(String testName, Method method) {
        super(testName);
        this.TDConfigAnnotatedMethod = method;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() { return new TestSuite(); }

    public void testModifier() {
        assertTrue("ToughDay2 config method \"" + TDConfigAnnotatedMethod + "\" must be public",
                (TDConfigAnnotatedMethod.getModifiers() & Modifier.PUBLIC) != 0);
    }

    public void testArguments() {
        assertTrue( "ToughDay2 config method \"" + TDConfigAnnotatedMethod + "\" must have only one parameter",
                TDConfigAnnotatedMethod.getParameterTypes().length == 1);

        assertTrue( "ToughDay2 config method's \"" + TDConfigAnnotatedMethod + "\" parameter must be of type String",
                TDConfigAnnotatedMethod.getParameterTypes()[0].isAssignableFrom(String.class));
    }
}
