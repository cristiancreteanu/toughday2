package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.AbstractTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Constructor;

/**
 * Created by tuicu on 13/06/16.
 */

@Category(TestTDConstraints.class)
public class TestConstructor extends TestCase {
    private Class TDClass;

    public TestConstructor(String testName, Class<? extends AbstractTest> TDTestClass) {
        super(testName);
        this.TDClass = TDTestClass;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() { return new TestSuite(); }

    public void test() throws NoSuchMethodException {
        try {
            Constructor constructor = TDClass.getConstructor(null);
        } catch (NoSuchMethodException e) {
            fail("ToughDay2 class \"" + TDClass.getName() + "\" doesn't have a public constructor with no arguments, or it is not public");
        }
    }
}
