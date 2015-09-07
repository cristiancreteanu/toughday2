package com.day.qa.toughday.core;

import com.day.qa.toughday.core.test_annotations.After;
import com.day.qa.toughday.core.test_annotations.Before;
import com.day.qa.toughday.core.test_annotations.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by tuicu on 25/08/15.
 */
public abstract class AbstractTestRunner<T extends AbstractTest> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestRunner.class);
    private boolean setupExecuted;
    private Method setupMethod;
    private Method beforeMethod;
    private Method afterMethod;

    public AbstractTestRunner(Class<? extends AbstractTest> testClass) {
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

    protected void executeSetup(AbstractTest testObject) {
        if(!setupExecuted) {
            synchronized (this) {
                if(!setupExecuted) {
                    setupExecuted = true;
                    executeMethod(testObject, setupMethod, Setup.class);
                }
            }
        }
    }

    protected void executeBefore(AbstractTest testObject) {
        if(beforeMethod != null) {
            executeMethod(testObject, beforeMethod, Before.class);
        }
    }

    protected void executeAfter(AbstractTest testObject) {
        if(afterMethod != null) {
            executeMethod(testObject, afterMethod, After.class);
        }
    }

    public void runTest(AbstractTest testObject, RunMap runMap) {
        executeSetup(testObject);
        executeBefore(testObject);

        run( (T) testObject, runMap);

        executeAfter(testObject);
    }

    protected abstract void run(T testObject, RunMap runMap);

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
