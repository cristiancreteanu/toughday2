package com.adobe.qe.toughday.structural;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by tuicu on 13/06/16.
 */

@Category(TestTDConstraints.class)
public class TestConfigSetAnnotatedMethod extends TestCase {
    private Method TdConfigAnnotatedMethod;

    public TestConfigSetAnnotatedMethod(String testName, Method method) {
        super(testName);
        this.TdConfigAnnotatedMethod = method;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() { return new TestSuite(); }

    public void testModifier() {
        assertTrue("ToughDay2 config method \"" + TdConfigAnnotatedMethod + "\" must be public",
                (TdConfigAnnotatedMethod.getModifiers() & Modifier.PUBLIC) != 0);
    }

    public void testArguments() {
        assertTrue( "ToughDay2 config method \"" + TdConfigAnnotatedMethod + "\" must have only one parameter",
                TdConfigAnnotatedMethod.getParameterTypes().length == 1);

        assertTrue( "ToughDay2 config method's \"" + TdConfigAnnotatedMethod + "\" parameter must be of type String",
                TdConfigAnnotatedMethod.getParameterTypes()[0].isAssignableFrom(String.class));
    }
}
