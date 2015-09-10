package com.day.qa.toughday.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by tuicu on 09/09/15.
 */
public class RunnersContainer {
    private static final Logger logger = LoggerFactory.getLogger(RunnersContainer.class);
    private static RunnersContainer instance = new RunnersContainer();
    public static RunnersContainer getInstance() { return instance; }

    private HashMap<Class<? extends AbstractTest>, AbstractTestRunner> testRunners;
    private RunnersContainer() {
        this.testRunners = new HashMap<>();
    }

    public void addRunner(AbstractTest test)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        if(!testRunners.containsKey(test.getClass())) {
            Class<? extends AbstractTestRunner> runnerClass = test.getTestRunnerClass();
            try {
                Constructor<? extends AbstractTestRunner> constructor = runnerClass.getConstructor(Class.class);
                testRunners.put(test.getClass(), constructor.newInstance(test.getClass()));
            } catch (NoSuchMethodException e) {
                logger.error("Cannot run test " + test.getName() + " because the runner doesn't have the appropriate constructor");
                throw new NoSuchMethodException("Test runners must have a constructor with only one parameter, the test Class");
            }
        }
    }

    public AbstractTestRunner getRunner(AbstractTest test) {
        return testRunners.get(test.getClass());
    }
}
