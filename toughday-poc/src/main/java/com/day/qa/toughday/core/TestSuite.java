package com.day.qa.toughday.core;

import com.day.qa.toughday.core.config.ConfigArg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by tuicu on 12/08/15.
 */
public class TestSuite {
    private SuiteSetup setupStep;
    private WeightMap weightMap;

    private static class WeightMap extends HashMap<AbstractTest, Integer> {
        private int totalWeight;

        @Override
        public Integer put(AbstractTest test, Integer weight) {
            Integer previous = super.put(test, weight);
            totalWeight += -(previous != null ? previous : 0) + weight;
            return previous;
        }

        @Override
        public Integer remove(Object test) {
            Integer previous = super.remove(test);
            totalWeight -= (previous != null ? previous : 0);
            return previous;
        }

        public int getTotalWeight() {
            return totalWeight;
        }
    }


    public TestSuite() {
        weightMap = new WeightMap();
    }

    public TestSuite add(AbstractTest test, int weight) {
        weightMap.put(test, weight);
        return this;
    }


    public TestSuite addAll(TestSuite testSuite) {
        this.weightMap.putAll(testSuite.weightMap);
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


    public SuiteSetup getSetupStep() {
        return setupStep;
    }


    public HashMap<AbstractTest, Integer> getWeightMap() {
        return weightMap;
    }

    public int getTotalWeight() {
        return weightMap.getTotalWeight();
    }

    public Set<AbstractTest> getTests() {
        return weightMap.keySet();
    }
}

