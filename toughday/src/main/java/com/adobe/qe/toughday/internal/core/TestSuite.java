package com.adobe.qe.toughday.internal.core;

import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.AbstractTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Test suite class.
 */
public class TestSuite {
    private List<SuiteSetup> setupStep;
    private WeightMap weightMap;
    private HashMap<AbstractTest, Long> timeoutMap;
    private HashMap<AbstractTest, Long> counterMap;
    private String description = "";
    private List<String> tags = new ArrayList<>();
    private List<AbstractTest> orderedTests;


    /**
     * Weight map class.
     */
    private static class WeightMap extends HashMap<AbstractTest, Integer> {
        private int totalWeight;
        private HashMap<String, AbstractTest> nameMap = new HashMap<>();

        /**
         * Puts the test in the map with the associated weight and returns the previous weight.
         * @param test
         * @param weight
         * @return
         */
        @Override
        public Integer put(AbstractTest test, Integer weight) {
            if (nameMap.containsKey(test.getName())) {
                throw new IllegalArgumentException("Suite already contains a test named: \"" + test.getName() + "\". " +
                        "Please provide a different name using the \"name\" property.");
            }

            nameMap.put(test.getName(), test);
            Integer previous = super.put(test, weight);
            totalWeight += -(previous != null ? previous : 0) + weight;
            return previous;
        }

        @Override
        public void putAll(Map<? extends AbstractTest, ? extends Integer> other) {
            for(Map.Entry<? extends AbstractTest, ? extends Integer> entry : other.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }

        /**
         * Removes the test from the map.
         * @param test
         * @return
         */
        @Override
        public Integer remove(Object test) {
            if(!(test instanceof AbstractTest))
                throw new IllegalArgumentException("Argument must extend AbstractTest");
            nameMap.remove(((AbstractTest) test).getName());

            Integer previous = super.remove(test);
            totalWeight -= (previous != null ? previous : 0);
            return previous;
        }

        /**
         * Getter for the total weight.
         * @return
         */
        public int getTotalWeight() {
            return totalWeight;
        }

        public AbstractTest getTest(String testName) {
            if(!nameMap.containsKey(testName))
                throw new IllegalArgumentException("Test suite doesn't contain a test with this name: " + testName);
            return nameMap.get(testName);
        }

        public boolean contains(String testName) {
            return nameMap.containsKey(testName);
        }
    }

    /**
     * Constructor.
     */
    public TestSuite() {
        weightMap = new WeightMap();
        timeoutMap = new HashMap<>();
        counterMap = new HashMap<>();
        setupStep = new ArrayList<>();
        orderedTests = new ArrayList<>();
    }

    /**
     * Method for adding a test with the weight.
     * @param test
     * @param weight
     * @return this object. (builder pattern)
     */
    public TestSuite add(AbstractTest test, int weight) {
        weightMap.put(test, weight);
        orderedTests.add(test);
        return this;
    }

    /**
     * Method for adding a test with weight, timeout and a count
     * @param test the test to be executed
     * @param weight the weight of this test compared to other tests in this suite
     * @param timeout the time it takes for a test to be considered timed-out
     * @param count Maximum number of executions
     * @return
     */
    public TestSuite add(AbstractTest test, int weight, long timeout, long count) {
        add(test, weight);
        if (timeout >= 0) {
            timeoutMap.put(test, timeout * 1000);
        }
        if (count >= 0) {
            counterMap.put(test, count);
        }
        return this;
    }

    /**
     * Method for replacing the weight for a test
     * @param testName
     * @param weight
     * @return
     */
    public TestSuite replaceWeight(String testName, int weight) {
        AbstractTest test = weightMap.getTest(testName);
        weightMap.put(test, weight);
        return this;
    }

    /**
     * Method for replacing the timeout for a test
     * @param testName
     * @param timeout
     * @return
     */
    public TestSuite replaceTimeout(String testName, long timeout) {
        AbstractTest test = weightMap.getTest(testName);
        timeoutMap.put(test, timeout);
        return this;
    }

    /**
     * Method for replacing the max count for a test
     * @param testName
     * @param count
     * @return
     */
    public TestSuite replaceCount(String testName, long count) {
        AbstractTest test = weightMap.getTest(testName);
        counterMap.put(test, count);
        return this;
    }

    /**
     * Method for replacing the name of a test inside the suite.
     * @param abstractTest
     * @param newTestName
     * @return
     */
    public TestSuite replaceName(AbstractTest abstractTest, String newTestName) {
        int weight = weightMap.remove(abstractTest);
        abstractTest.setName(newTestName);
        weightMap.put(abstractTest, weight);
        return this;
    }

    /**
     * Getter for the setup step.
     * @return a SetupStep object if configured, null otherwise.
     */
    public TestSuite addAll(TestSuite testSuite) {
        this.weightMap.putAll(testSuite.getWeightMap());
        this.timeoutMap.putAll(testSuite.timeoutMap);
        this.counterMap.putAll(testSuite.counterMap);
        this.setupStep.addAll(testSuite.setupStep);
        this.orderedTests.addAll(testSuite.orderedTests);
        return this;
    }

    /**
     * Setter for the setup step, as seen from the configuration.
     * @param setupStepClassNames a list with suite setup names separated by commas
     * @return this object. (builder pattern)
     * @throws ClassNotFoundException caused by reflection
     * @throws NoSuchMethodException caused by reflection
     * @throws IllegalAccessException caused by reflection
     * @throws InvocationTargetException caused by reflection
     * @throws InstantiationException caused by reflection
     */
    @ConfigArgSet(required = false)
    public TestSuite setSuiteSetup(String setupStepClassNames)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        String[] suiteSetups = setupStepClassNames.split(",");

        for (String setupStepClassName : suiteSetups) {
            Class<? extends SuiteSetup> setupStepClass = null;
            setupStepClass = ReflectionsContainer.getInstance().getSuiteSetupClasses().get(setupStepClassName);

            if (setupStepClass == null) {
                throw new ClassNotFoundException("Could not find class " + setupStepClassName + " for suite setup step");
            }
            withSetupStep(setupStepClass);
        }
        return this;
    }

    /**
     * Overload of withSetupStep.
     * @param setupStepClass
     * @return this object. (builder pattern)
     * @throws ClassNotFoundException caused by reflection
     * @throws NoSuchMethodException caused by reflection
     * @throws IllegalAccessException caused by reflection
     * @throws InvocationTargetException caused by reflection
     * @throws InstantiationException caused by reflection
     */
    public TestSuite withSetupStep(Class<? extends SuiteSetup> setupStepClass)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Constructor constructor = setupStepClass.getConstructor(null);
        withSetupStep((SuiteSetup) constructor.newInstance());
        return this;
    }

    /**
     * Overload of withSetupStep.
     * @param setupStep
     * @return
     */
    public TestSuite withSetupStep(SuiteSetup setupStep) {
        this.setupStep.add(setupStep);
        return this;
    }

    /**
     * Method for setting the description
     * @param description
     * @return
     */
    public TestSuite setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method for adding a tag
     * @param tag
     * @return
     */
    public TestSuite addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    /**
     * Method for adding tags
     * @param tags
     * @return
     */
    public TestSuite addTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }

    /**
     * Method for adding tags
     * @param tags
     * @return
     */
    public TestSuite addTags(String[] tags) {
        this.tags.addAll(Arrays.asList(tags));
        return this;
    }

    /**
     * Getter for the tags
     * @return
     */
    public List<String> getTags() { return tags; }

    /**
     * Getter for the setup step.
     * @return a SetupStep object if configured, null otherwise.
     */
    public List<SuiteSetup> getSetupStep() {
        return setupStep;
    }

    /**
     * Method for getting the timeout for a specific test.
     * @param test
     * @return the timeout if configured, null otherwise.
     */
    public Long getTimeout(AbstractTest test) {
        return timeoutMap.get(test);
    }

    /**
     * Method for getting the count for a specific test.
     * @param test
     * @return the timeout if configured, null otherwise.
     */
    public Long getCount(AbstractTest test) {
        return counterMap.get(test);
    }

    /**
     * Getter for the weight map.
     * @return
     */
    public Map<AbstractTest, Integer> getWeightMap() {
        return weightMap;
    }

    /**
     * Getter for the total weight.
     */
    public int getTotalWeight() {
        return weightMap.getTotalWeight();
    }

    /**
     * Getter for the test set.
     */
    public Collection<AbstractTest> getTests() {
        return orderedTests;
    }

    /**
     * Getter for a name given a test
     * @param testName
     * @return
     */
    public AbstractTest getTest(String testName) {
        return weightMap.getTest(testName);
    }

    /**
     * Method for removing a test given it's name
     * @param testName
     */
    public void remove(String testName) {
        AbstractTest test = weightMap.getTest(testName);
        orderedTests.remove(test);
        weightMap.remove(test);
    }

    /**
     * Method for removing a test
     * @param test
     */
    public void remove(AbstractTest test) {
        weightMap.remove(test);
        orderedTests.remove(test);
    }

    /**
     * Method for finding if the suite contains a test with the given name
     * @param testName
     * @return
     */
    public boolean contains(String testName) {
        return weightMap.contains(testName);
    }
}
