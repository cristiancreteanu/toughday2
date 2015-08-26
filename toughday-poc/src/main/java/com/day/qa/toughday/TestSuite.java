package com.day.qa.toughday;

import com.day.qa.toughday.publishers.Publisher;
import com.day.qa.toughday.tests.AbstractTest;

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
                    System.err.println("Pool did not terminate");
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
    private HashMap<Class<? extends AbstractTest>, TestRunner> testRunners;

    HashMap<AbstractTest, Integer> weightMap;

    public TestSuite(int durationSec, int delay, int concurrency) {
        this.globalTestList = new ArrayList<>();
        this.delay = delay;
        this.concurrency = concurrency;
        this.executorService = Executors.newFixedThreadPool(concurrency + 1);
        this.duration = durationSec;
        this.weightMap = new HashMap<>();
        this.globalRunMap = new RunMap();
        this.publishers = new ArrayList<>();
        this.testRunners = new HashMap<>();
    }

    public TestSuite add(AbstractTest test, int weight) {
        globalTestList.add(test);
        totalWeight += weight;
        weightMap.put(test, weight);
        globalRunMap.addTest(test);
        if(!testRunners.containsKey(test.getClass())) {
            testRunners.put(test.getClass(), new TestRunner(test.getClass()));
        }
        return this;
    }

    public TestSuite addPublisher(Publisher publisher) {
        publishers.add(publisher);
        return this;
    }
    
    public void runTests() {
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
            System.out.println("Thread running: " + Thread.currentThread());
            try {
                while (!finish) {
                    AbstractTest nextTest = getNextTest(localTests, totalWeight);
                    try {
                        TestRunner runner = testRunners.get(nextTest.getClass());
                        Long nanoSecElapsed = runner.runTest(nextTest);
                        synchronized (localRunMap) {
                            localRunMap.recordRun(nextTest, nanoSecElapsed);
                        }
                    } catch (Exception e) {
                        synchronized (localRunMap) {
                            localRunMap.recordFail(nextTest, e);
                        }
                    }
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
