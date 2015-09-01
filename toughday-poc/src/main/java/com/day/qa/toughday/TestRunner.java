package com.day.qa.toughday;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.tests.AbstractTest;
import com.day.qa.toughday.tests.annotations.After;
import com.day.qa.toughday.tests.annotations.Before;
import com.day.qa.toughday.tests.annotations.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by tuicu on 25/08/15.
 */
public class TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);
    private boolean setupExecuted;
    private Method setupMethod;
    private Method beforeMethod;
    private Method afterMethod;

    public TestRunner(Class<? extends AbstractTest> testClass) {
        setupExecuted = true;
        for (Method method : testClass.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == Setup.class) {
                    if (validateMethod(method, Setup.class)) {
                        setupMethod = method;
                        setupExecuted = false;
                    }
                } else if (annotation.annotationType() == Before.class) {
                    if (validateMethod(method, Before.class)) {
                        beforeMethod = method;
                    }
                } else if (annotation.annotationType() == After.class) {
                    if (validateMethod(method, After.class)) {
                        afterMethod = method;
                    }
                }
            }
        }
    }

    public long runTest(AbstractTest testObject) throws ClientException {
        if(!setupExecuted) {
            synchronized (this) {
                if(!setupExecuted) {
                    setupExecuted = true;
                    executeMethod(testObject, setupMethod, Setup.class);
                }
            }
        }
        if(beforeMethod != null) {
            executeMethod(testObject, beforeMethod, Before.class);
        }
        Long start = System.nanoTime();
        testObject.test();
        Long elapsed = (System.nanoTime() - start) / 1000000l;
        if(afterMethod != null) {
            executeMethod(testObject, afterMethod, After.class);
        }
        return elapsed;
    }

    private void executeMethod(AbstractTest testObject, Method method, Class<? extends Annotation> annotation) {
        try {
            method.invoke(testObject);
        } catch (IllegalAccessException e) {
            logger.error("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName() + ": Illegal Access.", e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName(), e);
            e.printStackTrace();
        }
    }

    private boolean validateMethod(Method method, Class<? extends Annotation> annotation) {
        if(method.getParameterTypes().length != 0) {
            logger.error("Method " + method + " annotated with " + annotation.getSimpleName() + " cannot have parameters");
            return false;
        }
        return true;
    }
}
