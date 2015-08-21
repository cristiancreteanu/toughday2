package com.day.qa.toughday;

import com.day.qa.toughday.tests.AbstractTest;

import java.util.*;
import java.util.concurrent.*;

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

    HashMap<AbstractTest, Integer> weightMap;

    public TestSuite(int durationSec, int delay, int concurrency) {
        this.globalTestList = new ArrayList<>();
        this.delay = delay;
        this.concurrency = concurrency;
        this.executorService = Executors.newFixedThreadPool(concurrency + 1);
        this.duration = durationSec;
        this.weightMap = new HashMap<>();
        this.globalRunMap = new RunMap();
    }

    public TestSuite add(AbstractTest test, int weight) {
        globalTestList.add(test);
        totalWeight += weight;
        weightMap.put(test, weight);
        globalRunMap.addTest(test);
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
        System.out.println("********************************************************************");
        System.out.println("                       FINAL RESULTS");
        System.out.println("********************************************************************");
        resultAggregator.showResults();
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
            try {
                while (!finish) {
                    AbstractTest nextTest = getNextTest(localTests, totalWeight);
                    try {
                        Long nanoSecElapsed = nextTest.runTest();
                        synchronized (localRunMap) {
                            localRunMap.recordRun(nextTest, nanoSecElapsed);
                        }
                    } catch (Exception e) {
                        synchronized (localRunMap) {
                            localRunMap.recordFail(nextTest);
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
                    showResults();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            aggregateResults();
        }

        public void showResults() {
            for(RunMap.TestStatistics statistics : globalRunMap.getTestStatistics()) {
                System.out.println(
                        "Test " + statistics.getTest().getName()
                                + " Total Duration: " + statistics.getTotalDuration()
                                + " Runs: " + statistics.getTotalRuns()
                                + " Fails: " + statistics.getFailRuns()
                                + " Min: " + statistics.getMinDuration()
                                + " Max: " + statistics.getMaxDuration()
                                + " Average: " + statistics.getAverageDuration()
                                + " Throughput: " + statistics.getThroughput()
                                + " Median: " + statistics.getMedianDuration()
                );
            }
        }
    }
}
