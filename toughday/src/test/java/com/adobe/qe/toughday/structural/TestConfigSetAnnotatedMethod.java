package com.adobe.qe.toughday.structural;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.benckmark.AdHocTest;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.config.Configuration;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;
import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by tuicu on 13/06/16.
 */

@Category(TestTDConstraints.class)
public class TestConfigSetAnnotatedMethod extends TestCase {
    private Method TdConfigAnnotatedMethod;
    private Class classType;

    public TestConfigSetAnnotatedMethod(String testName, Method method) {
        super(testName);
        this.TdConfigAnnotatedMethod = method;
    }

    public TestConfigSetAnnotatedMethod(String testName, Class classType) {
        super(testName);
        this.classType = classType;
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

    /* This test code was only required to make sure we don't break anything when implementing NPR-15849. However we're
       keeping it just a bit more, in case we needed. If we find that it doesn't help with anything, it can be deleted.
    /**
     *
     * @param testClass
     * @param testInstance
     * @return a map that contains the default value for each property that the test instance has.
     *

    private HashMap<String, String> callGetters(Class testClass, Object testInstance) {
        HashMap<String, String> defaultValues = new HashMap<>();
        for (Method method : testClass.getMethods()) {
            ConfigArgGet getAnnotation = method.getAnnotation(ConfigArgGet.class);
            if (getAnnotation == null) {
                continue;
            }

            String defaultValue = "";
            try {
                defaultValue = String.valueOf(method.invoke(testInstance));
            } catch (Throwable e) {
                fail("Getter fot property " + Configuration.propertyFromMethod(method.getName()) + " could not be invoked.");
            }

            if (!(defaultValue.compareTo("") == 0)) {
                String key = Configuration.propertyFromMethod(method.getName());
                defaultValues.put(key, defaultValue);
            }
        }

        return defaultValues;
    }

    /**
     * Sets the properties of the test to their default value.
     * @param classType
     * @param classInstance
     *

    private void callSetters(Class classType, Object classInstance) {

        if (classInstance instanceof CompositeTest) {
            for (AbstractTest test: ((CompositeTest) classInstance).getChildren()) {
                callSetters(test.getClass(), test);
            }
        }

        for (Method method : classType.getMethods()) {
            ConfigArgSet setAnnotation = method.getAnnotation(ConfigArgSet.class);

            if (setAnnotation == null) {
                continue;
            }

            String defaultValue = setAnnotation.defaultValue();
            if (defaultValue.compareTo("") == 0) {
                continue;
            }

            try {
                method.invoke(classInstance, defaultValue);
            } catch (Throwable e) {
                fail("Setter for property " + Configuration.propertyFromMethod(method.getName()) + " could not be invoked.");
            }
        }

    }

    /**
     *  This test verifies that setting the default and calling the setters with the default value has the same result.
     *

    public void testSettingDefaultValues() {

        if (Modifier.isAbstract(classType.getModifiers())) {
            return;
        }
        if(classType == AdHocTest.class) {
            return;
        }

        Constructor constructor = null;

        try {
            constructor = classType.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Object testInstance = null;

        try {
            if (!(constructor.isAccessible())) {
                constructor.setAccessible(true);
            }
            testInstance = constructor.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        HashMap<String, String> defaultValues = callGetters(classType, testInstance);
        callSetters(classType,testInstance);
        HashMap<String, String> settedValues = callGetters(classType, testInstance);

        for (String key : defaultValues.keySet()) {
            Assert.assertEquals("Default values differ for class " + classType.getName(),defaultValues.get(key),settedValues.get(key));
        }
    }
    */
}
