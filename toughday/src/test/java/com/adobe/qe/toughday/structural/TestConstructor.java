package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.internal.core.benckmark.AdHocTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Constructor;

/**
 * Created by tuicu on 13/06/16.
 */

@Category(TestTDConstraints.class)
public class TestConstructor extends TestCase {
    private Class TdClass;

    public TestConstructor(String testName, Class<? extends AbstractTest> TDTestClass) {
        super(testName);
        this.TdClass = TDTestClass;
    }

    /**
     *  Added to supress JUnit warning.
     *  The actual test suite is created in TestSuiteStructural
     *  */
    public static TestSuite suite() { return new TestSuite(); }

    public void test() throws NoSuchMethodException {
        //The AdHocTest is an exception from the rule
        if(TdClass == AdHocTest.class) {
            return;
        }

        try {
            Constructor constructor = TdClass.getConstructor(null);
        } catch (NoSuchMethodException e) {
            fail("ToughDay2 class \"" + TdClass.getName() + "\" doesn't have a public constructor with no arguments, or it is not public");
        }
    }
}
