package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base class of all runners. For each test only one runner will be instantiated by the Engine and placed into the
 * RunnersContainer. In this way it is ensured that the Setup method is executed only once, even though the test is
 * replicated for each thread. Aside from setup step execution, this class is not thread safe. Any other required
 * synchronization must be implemented by subtypes, but it could affect the throughput of the executed tests. Ideally
 * runners should have no state.
 * TODO: investigate what happens if there are multiple engines. Since they use the same container, Setup will not work correctly
 */
public abstract class AbstractTestRunner<T extends AbstractTest> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestRunner.class);
    private boolean setupExecuted;
    private Method setupMethod;
    private Method beforeMethod;
    private Method afterMethod;

    /**
     * Constructor
     * @param testClass
     * TODO: throw exceptions if not valid methods.
     */
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

    /**
     * Method for executing the setup method using reflection for the specified instance of the test class.
     * The setup is guaranteed to be executed once even if the test is replicated(cloned) for multiple threads.
     * @param testObject
     */
    protected void executeSetup(AbstractTest testObject) {
        /* The synchronized block, the second if and the assignation of the variable setupExecuted only after
        the call of the method, are to ensure that the setup is executed exactly once, even if this runner is used
        by multiple threads. The first if is to ensure that no bottleneck occurs due to synchronization. */
        if (!setupExecuted) {
            synchronized (this) {
                if (!setupExecuted) {
                    executeMethod(testObject, setupMethod, Setup.class);
                    setupExecuted = true;
                }
            }
        }
    }

    /**
     * Method for executing the before method using reflection for the specified instance of the test class.
     * It will run before each test run.
     * @param testObject
     */
    protected void executeBefore(AbstractTest testObject) {
        if (beforeMethod != null) {
            executeMethod(testObject, beforeMethod, Before.class);
        }
    }

    /**
     * Method for executing the after method using reflection for the specified instance of the test class.
     * The after method is guaranteed to run after every test run, even if exceptions occur.
     * @param testObject
     */
    protected void executeAfter(AbstractTest testObject) {
        if (afterMethod != null) {
            executeMethod(testObject, afterMethod, After.class);
        }
    }

    /**
     * Runs a test an benchmarks its execution.
     * @param testObject instance of the test to run
     * @param runMap the run map in which the benchmark will be recorded.
     * @throws ChildTestFailedException propagated exception if the test object is part of a composite test.
     */
    public void runTest(AbstractTest testObject, RunMap runMap) throws ChildTestFailedException {
        executeSetup(testObject);
        executeBefore(testObject);

        ChildTestFailedException exception = null;
        try {
            run((T) testObject, runMap);
        } catch (ChildTestFailedException e) {
            exception = e;
        }

        executeAfter(testObject);

        if(exception != null) {
            throw exception;
        }
    }

    /**
     * Method for delegating the responsability of correctly running and benchmarking the test to subclasses.
     * @param testObject instance of the test to run
     * @param runMap the run map in which the benchmark will be recorded.
     * @throws ChildTestFailedException propagated exception if the test object is part of a composite test.
     */
    protected abstract void run(T testObject, RunMap runMap) throws ChildTestFailedException;

    /**
     * Run a annotated method using reflections.
     * @param testObject instance of the test for which the method will be executed
     * @param method that will be invoked by using reflections
     * @param annotation what annotation caused this method to be ran
     */
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

    /**
     * Validator for methods annotated with test_annotations.
     * @param method
     * @param annotation
     * @return true if the method is valid, false if not.
     */
    private boolean validateMethod(Method method, Class<? extends Annotation> annotation) {
        if(method.getParameterTypes().length != 0) {
            logger.error("Method " + method + " annotated with " + annotation.getSimpleName() + " cannot have parameters");
            return false;
        }
        return true;
    }
}