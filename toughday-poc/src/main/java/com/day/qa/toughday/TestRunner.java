package com.day.qa.toughday;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.tests.AbstractTest;
import com.day.qa.toughday.tests.annotations.After;
import com.day.qa.toughday.tests.annotations.Before;
import com.day.qa.toughday.tests.annotations.Setup;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by tuicu on 25/08/15.
 */
public class TestRunner {
    private boolean setupExecuted;
    private Method setupMethod;
    private Method beforeMethod;
    private Method afterMethod;

    public TestRunner(Class<? extends AbstractTest> testClass) {
        setupExecuted = true;
        for (Method method : testClass.getClass().getMethods()) {
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
                    executeMethod(setupMethod, Setup.class);
                }
            }
        }
        if(beforeMethod != null) {
            executeMethod(beforeMethod, Before.class);
        }
        Long start = System.nanoTime();
        testObject.test();
        Long elapsed = (System.nanoTime() - start) / 1000000l;
        if(afterMethod != null) {
            executeMethod(afterMethod, After.class);
        }
        return elapsed;
    }

    private void executeMethod(Method method, Class<? extends Annotation> annotation) {
        try {
            method.invoke(this);
        } catch (IllegalAccessException e) {
            System.out.println("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName() + ": Illegal Access.");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName());
            e.printStackTrace();
        }
    }

    private boolean validateMethod(Method method, Class<? extends Annotation> annotation) {
        if(method.getParameterTypes().length != 0) {
            System.out.println("Method " + method + " annotated with " + annotation.getSimpleName() + " cannot have parameters");
            return false;
        }
        return true;
    }
}
