package com.day.qa.toughday.core;

import com.day.qa.toughday.core.config.ConfigArg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tuicu on 12/08/15.
 */
public class TestSuite {
    private SuiteSetup setupStep;
    private List<AbstractTest> testList;
    private int totalWeight;
    private HashMap<AbstractTest, Integer> weightMap;
    private int concurrency;
    private int waitTime;
    private int duration;
    private List<Publisher> publishers;
    private int timeout;

    public TestSuite() {
        testList = new ArrayList<>();
        weightMap = new HashMap<>();
        publishers = new ArrayList<>();
    }

    public TestSuite add(AbstractTest test, int weight) {
        testList.add(test);
        totalWeight += weight;
        weightMap.put(test, weight);
        return this;
    }

    @ConfigArg(required = false)
    public TestSuite setSetupStep(String setupStepClassName)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        Class<? extends SuiteSetup> setupStepClass = null;
        setupStepClass = ReflectionsContainer.getInstance().getSuiteSetupClasses().get(setupStepClassName);

        if (setupStepClass == null) {
            throw new ClassNotFoundException("Could not find class " + setupStepClassName + " for suite setup step");
        }
        setSetupStep(setupStepClass);
        return this;
    }


    public TestSuite setSetupStep(Class<? extends SuiteSetup> setupStepClass)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Constructor constructor = setupStepClass.getConstructor(null);
        this.setupStep = (SuiteSetup) constructor.newInstance();
        return this;
    }

    public TestSuite setSetupStep(SuiteSetup setupStep) {
        this.setupStep = setupStep;
        return this;
    }

    @ConfigArg
    public TestSuite setConcurrency(String concurrencyString) {
        this.concurrency = Integer.parseInt(concurrencyString);
        return this;
    }

    @ConfigArg
    public TestSuite setDuration(String durationString) {
        this.duration = Integer.parseInt(durationString);
        return this;
    }

    @ConfigArg
    public TestSuite setWaitTime(String waitTime) {
        this.waitTime = Integer.parseInt(waitTime);
        return this;
    }

    @ConfigArg
    public TestSuite setTimeout(String timeout) {
        this.timeout = Integer.parseInt(timeout) * 1000;
        return this;
    }

    public TestSuite addPublisher(Publisher publisher) {
        publishers.add(publisher);
        return this;
    }

    public HashMap<AbstractTest, Integer> getWeightMap() {
        return weightMap;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getDuration() {
        return duration;
    }

    public SuiteSetup getSetupStep() {
        return setupStep;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public List<AbstractTest> getTests() {
        return testList;
    }

    public int getTimeout() {
        return timeout;
    }
}

