package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.CloneSetup;
import com.adobe.qe.toughday.core.annotations.Setup;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by tuicu on 13/06/16.
 */

@Category(TestTDConstraints.class)
public class TestAnnotatedMethod extends TestCase {
    private Method TdAnnotatedMethod;

    public TestAnnotatedMethod(String testName, Method method) {
        super(testName);
        this.TdAnnotatedMethod = method;
    }

    public static boolean hasAnnotation(Method method) {
        return method.getAnnotation(Before.class) != null
                || method.getAnnotation(After.class) != null
                || method.getAnnotation(CloneSetup.class) != null
                || method.getAnnotation(Setup.class) != null;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() {
        return new TestSuite();
    }

    public void testModifier() {
        assertTrue("ToughDay2 annotated method \"" + TdAnnotatedMethod + "\" must be final or private",
                Modifier.isFinal(TdAnnotatedMethod.getModifiers()) || Modifier.isPrivate(TdAnnotatedMethod.getModifiers()));
    }

    public void testArguments() {
        assertTrue("ToughDay2 annotated method \"" + TdAnnotatedMethod + "\" is not allowed to have parameters",
                TdAnnotatedMethod.getParameterTypes().length == 0);
    }
}
