package com.day.qa.toughday;

import com.day.qa.toughday.cli.CliArg;
import com.day.qa.toughday.publishers.Publisher;
import com.day.qa.toughday.tests.AbstractTest;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuicu on 12/08/15.
 */
public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);
    private static final int RESULT_AGGREATION_DELAY = 1000; //in ms
    private static final int WAIT_TERMINATION_FACTOR = 3;
    private static Random _rnd = new Random();

    private AbstractTest getNextTest(List<AbstractTest> tests, int totalWeight) {
        int randomNumber = _rnd.nextInt(totalWeight);

        AbstractTest selectedTest = null;
        for (AbstractTest test : tests) {
            int testWeight = weightMap.get(test);
            if (randomNumber < testWeight) {
                selectedTest = test;
                break;
            }
            randomNumber = randomNumber - testWeight;
        }

        return selectedTest;
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.SECONDS))
                    logger.error("Thread pool did not terminate. Process must be killed");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private int duration;
    private List<AbstractTest> globalTestList;
    private int totalWeight;
    private int delay;
    private int concurrency;
    private ExecutorService executorService;
    private RunMap globalRunMap;
    private List<Publisher> publishers;
    private HashMap<Class<? extends AbstractTest>, AbstractTestRunner> testRunners;
    private SuiteSetup setupStep;

    HashMap<AbstractTest, Integer> weightMap;

    public TestSuite() {
        this.globalTestList = new ArrayList<>();
        this.weightMap = new HashMap<>();
        this.publishers = new ArrayList<>();
        this.testRunners = new HashMap<>();
    }

    @CliArg
    public TestSuite setConcurrency(String concurrencyString) {
        this.concurrency = Integer.parseInt(concurrencyString);
        this.executorService = Executors.newFixedThreadPool(concurrency + 1);
        this.globalRunMap = new RunMap(concurrency);
        return this;
    }

    @CliArg
    public TestSuite setDuration(String durationString) {
        this.duration = Integer.parseInt(durationString);
        return this;
    }

    @CliArg
    public TestSuite setWaitTime(String waitTime) {
        this.delay = Integer.parseInt(waitTime);
        return this;
    }

    @CliArg(required = false)
    public TestSuite setSetupStep(String setupStep)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Reflections reflections = new Reflections("com.day.qa");
        Class<? extends SuiteSetup> setupStepClass = null;
        for(Class<? extends SuiteSetup> klass : reflections.getSubTypesOf(SuiteSetup.class)) {
            if(klass.getSimpleName().equals(setupStep)) {
                setupStepClass = klass;
                break;
            }
        }
        if(setupStepClass == null) {
            throw new ClassNotFoundException("Could not find class " + setupStep + " for suite setup step");
        }
        Constructor constructor = setupStepClass.getConstructor(null);
        this.setupStep = (SuiteSetup) constructor.newInstance();
        return this;
    }

    public TestSuite add(AbstractTest test, int weight)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        globalTestList.add(test);
        totalWeight += weight;
        weightMap.put(test, weight);
        globalRunMap.addTest(test);
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
        return this;
    }

    public TestSuite addPublisher(Publisher publisher) {
        publishers.add(publisher);
        return this;
    }
    
    public void runTests() throws Exception {
        if(setupStep != null) {
            setupStep.setup();
        }
        List<AsyncTestRunner> testRunners = new ArrayList<>();
        for(int i = 0; i < concurrency; i++) {
            AsyncTestRunner runner = new AsyncTestRunner(globalRunMap.newInstance());
            testRunners.add(runner);
            executorService.execute(runner);
        }
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(testRunners);
        executorService.execute(resultAggregator);
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            for(AsyncTestRunner run : testRunners)
                run.finishExecution();
            resultAggregator.finishExecution();
        }
        shutdownAndAwaitTermination(executorService);
        publishFinalResults();
    }

    private void publishFinalResults() {
        for(Publisher publisher : publishers) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }

    private abstract class AsyncTestSuiteRunner implements Runnable {
        protected boolean finish = false;

        public void finishExecution() {
            finish = true;
        }
    }

    private class AsyncTestRunner extends AsyncTestSuiteRunner {
        private RunMap localRunMap;
        private List<AbstractTest> localTests;

        public AsyncTestRunner(RunMap localRunMap) {
            localTests = new ArrayList<>();
            for(AbstractTest test : globalTestList) {
                AbstractTest localTest = test.newInstance();
                localTest.setID(test.getId());
                localTests.add(localTest);
            }
            this.localRunMap = localRunMap;
        }

        public RunMap getLocalRunMap() {
            return localRunMap;
        }

        @Override
        public void run() {
            logger.info("Thread running: " + Thread.currentThread());
            try {
                while (!finish) {
                    AbstractTest nextTest = getNextTest(localTests, totalWeight);
                    AbstractTestRunner runner = testRunners.get(nextTest.getClass());
                    runner.runTest(nextTest, localRunMap);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class AsyncResultAggregator extends AsyncTestSuiteRunner {
        private List<AsyncTestRunner> testRunners;

        public AsyncResultAggregator(List<AsyncTestRunner> testRunners) {
            this.testRunners = testRunners;
        }
        
        private void aggregateResults() {
            for(AsyncTestRunner runner : testRunners) {
                RunMap localRunMap = runner.getLocalRunMap();
                synchronized (globalRunMap) {
                    synchronized (localRunMap) {
                        globalRunMap.aggregateAndReinitialize(localRunMap);
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                while(!finish) {
                    Thread.sleep(RESULT_AGGREATION_DELAY);
                    aggregateResults();
                    publishIntermediateResults();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            aggregateResults();
        }

        public void publishIntermediateResults() {
            for(Publisher publisher : publishers) {
                publisher.publishIntermediate(globalRunMap.getTestStatistics());
            }
        }
    }
}
