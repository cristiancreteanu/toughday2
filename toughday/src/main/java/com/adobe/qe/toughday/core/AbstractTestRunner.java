package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.CloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

/**
 * Base class of all runners. For each test only one runner will be instantiated by the Engine and placed into the
 * RunnersContainer. In this way it is ensured that the CloneSetup method is executed only once, even though the test is
 * replicated for each thread. Aside from setup step execution, this class is not thread safe. Any other required
 * synchronization must be implemented by subtypes, but it could affect the throughput of the executed tests. Ideally
 * runners should have no state.
 * TODO: investigate what happens if there are multiple engines. Since they use the same container, CloneSetup will not work correctly
 */
public abstract class AbstractTestRunner<T extends AbstractTest> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestRunner.class);
    private volatile boolean cloneSetupExecuted;
    private Method[] setupMethods;
    private Method[] beforeMethods;
    private Method[] afterMethods;

    /**
     * Constructor
     * @param testClass
     * TODO: throw exceptions if not valid methods.
     */
    public AbstractTestRunner(Class<? extends AbstractTest> testClass) {
        cloneSetupExecuted = true;
        LinkedList<Method> setupMethodList = new LinkedList<>();
        LinkedList<Method> beforeMethodList = new LinkedList<>();
        LinkedList<Method> afterMethodList = new LinkedList<>();

        Class currentClass = testClass;

        while(!currentClass.getName().equals(AbstractTest.class.getName())) {
            //System.out.println(currentClass.getName()); // ?
            for (Method method : currentClass.getDeclaredMethods()) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType() == CloneSetup.class) {
                        if (validateMethod(method, CloneSetup.class)) {
                            setupMethodList.addFirst(method);
                            method.setAccessible(true);
                            cloneSetupExecuted = false;
                        }
                    } else if (annotation.annotationType() == Before.class) {
                        if (validateMethod(method, Before.class)) {
                            method.setAccessible(true);
                            beforeMethodList.addFirst(method);
                        }
                    } else if (annotation.annotationType() == After.class) {
                        if (validateMethod(method, After.class)) {
                            method.setAccessible(true);
                            afterMethodList.addLast(method);
                        }
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (setupMethodList.size() > 0)
            this.setupMethods = setupMethodList.toArray(new Method[setupMethodList.size()]);

        if (beforeMethodList.size() >0)
            this.beforeMethods = beforeMethodList.toArray(new Method[beforeMethodList.size()]);

        if (afterMethodList.size() > 0)
            this.afterMethods = afterMethodList.toArray(new Method[afterMethodList.size()]);
    }

    /**
     * Method for executing the setup method using reflection for the specified instance of the test class.
     * The setup is guaranteed to be executed once even if the test is replicated(cloned) for multiple threads.
     * @param testObject
     */
    protected void executeCloneSetup(AbstractTest testObject) {
        /* The synchronized block, the second if and the assignation of the variable cloneSetupExecuted only after
        the call of the method, are to ensure that the setup is executed exactly once, even if this runner is used
        by multiple threads. The first if is to ensure that no bottleneck occurs due to synchronization. */
        if (!cloneSetupExecuted) {
            synchronized (this) {
                if (!cloneSetupExecuted) {
                    executeMethods(testObject, setupMethods, CloneSetup.class);
                    cloneSetupExecuted = true;
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
        if (beforeMethods != null) {
            executeMethods(testObject, beforeMethods, Before.class);
        }
    }

    /**
     * Method for executing the after method using reflection for the specified instance of the test class.
     * The after method is guaranteed to run after every test run, even if exceptions occur.
     * @param testObject
     */
    protected void executeAfter(AbstractTest testObject) {
        if (afterMethods != null) {
            executeMethods(testObject, afterMethods, After.class);
        }
    }

    /**
     * Runs a test an benchmarks its execution.
     * @param testObject instance of the test to run
     * @param runMap the run map in which the benchmark will be recorded.
     * @throws ChildTestFailedException propagated exception if the test object is part of a composite test.
     */
    public void runTest(AbstractTest testObject, RunMap runMap) throws ChildTestFailedException, SkippedTestException {
        executeCloneSetup(testObject);
        executeBefore(testObject);

        ChildTestFailedException childTestException = null;
        SkippedTestException skippedTestException = null;
        try {
            run((T) testObject, runMap);
        } catch ( ChildTestFailedException e) {
            childTestException = e;
        } catch (SkippedTestException e) {
            skippedTestException = e;
        }

        executeAfter(testObject);

        if(childTestException != null) {
            throw  childTestException;
        } else if (skippedTestException != null) {
            throw skippedTestException;
        }
    }

    /**
     * Method for delegating the responsability of correctly running and benchmarking the test to subclasses.
     * @param testObject instance of the test to run
     * @param runMap the run map in which the benchmark will be recorded.
     * @throws ChildTestFailedException propagated exception if the test object is part of a composite test.
     */
    protected abstract void run(T testObject, RunMap runMap) throws ChildTestFailedException, SkippedTestException;

    /**
     * Run a annotated method using reflections.
     * @param testObject instance of the test for which the method will be executed
     * @param methods that will be invoked by using reflections
     * @param annotation what annotation caused this method to be ran
     */
    private void executeMethods(AbstractTest testObject, Method[] methods, Class<? extends Annotation> annotation) {
        for (Method method : methods) {
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

        if((method.getModifiers() & Modifier.PRIVATE) == 0) {
            logger.error("Method " + method + " annotated with " + annotation.getSimpleName() + " must be private");
            return false;
        }
        return true;
    }
}
