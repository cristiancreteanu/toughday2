package com.day.qa.toughday.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuicu on 17/09/15.
 */
public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    private static final int RESULT_AGGREATION_DELAY = 1000; //in ms
    private static final int WAIT_TERMINATION_FACTOR = 3;
    private static final double TIMEOUT_CHECK_FACTOR = 0.5;
    private static Random _rnd = new Random();

    private AbstractTest getNextTest(TestSuite testSuite) {
        int randomNumber = _rnd.nextInt(testSuite.getTotalWeight());

        AbstractTest selectedTest = null;
        for (AbstractTest test : testSuite.getTests()) {
            int testWeight = testSuite.getWeightMap().get(test);
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

    private TestSuite testSuite;
    private ExecutorService executorService;
    private RunMap globalRunMap;

    public Engine(TestSuite testSuite)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.testSuite = testSuite;
        this.executorService = Executors.newFixedThreadPool(GlobalArgs.getInstance().getConcurrency() + 2);
        this.globalRunMap = new RunMap(GlobalArgs.getInstance().getConcurrency());
        for(AbstractTest test : testSuite.getTests()) {
            add(test);
        }
    }


    private Engine add(AbstractTest test)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        globalRunMap.addTest(test);
        RunnersContainer.getInstance().addRunner(test);
        for(AbstractTest child : test.getChildren()) {
            add(child);
        }
        return this;
    }

    public void runTests() throws Exception {
        if(testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for(int i = 0; i < GlobalArgs.getInstance().getConcurrency(); i++) {
            AsyncTestWorker runner = new AsyncTestWorker(testSuite, globalRunMap.newInstance());
            testWorkers.add(runner);
            executorService.execute(runner);
        }
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(testWorkers);
        executorService.execute(resultAggregator);
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(testSuite, testWorkers);
        executorService.execute(timeoutChecker);
        try {
            Thread.sleep(GlobalArgs.getInstance().getDuration() * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            for(AsyncTestWorker run : testWorkers)
                run.finishExecution();
            resultAggregator.finishExecution();
        }
        shutdownAndAwaitTermination(executorService);
        publishFinalResults();
    }

    private void publishFinalResults() {
        for(Publisher publisher : GlobalArgs.getInstance().getPublishers()) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }

    private abstract class AsyncEngineWorker implements Runnable {
        protected boolean finish = false;

        public void finishExecution() {
            finish = true;
        }
    }

    private class AsyncTestWorker extends AsyncEngineWorker {
        private RunMap localRunMap;
        private List<AbstractTest> localTests;
        private Thread workerThread;
        private long lastTestStart;
        private boolean testRunning;
        private TestSuite testSuite;

        public AsyncTestWorker(TestSuite testSuite, RunMap localRunMap) {
            this.testSuite = testSuite;
            localTests = new ArrayList<>();
            for(AbstractTest test : testSuite.getTests()) {
                AbstractTest localTest = test.clone();
                localTests.add(localTest);
            }
            this.localRunMap = localRunMap;
        }

        public Thread getWorkerThread() {
            return workerThread;
        }

        public boolean isTestRunning() {
            return testRunning;
        }

        public long getLastTestStart() {
            return lastTestStart;
        }

        public RunMap getLocalRunMap() {
            return localRunMap;
        }

        @Override
        public void run() {
            workerThread = Thread.currentThread();
            logger.info("Thread running: " + workerThread);
            try {
                while (!finish) {
                    AbstractTest nextTest = getNextTest(this.testSuite);
                    AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(nextTest);

                    try {
                        lastTestStart = System.nanoTime();
                        testRunning = true;
                        runner.runTest(nextTest, localRunMap);
                        testRunning = false;
                        /* It is possible for the timeout mechanism (see AsyncTimeoutChecker) to interrupt this thread
                         even when the test has finished running, but the thread didn't have time to update its state.
                         In that case we clear the interrupted flag, so it doesn't affect the execution of the next test
                         */
                        Thread.interrupted();
                    } catch (ChildTestFailedException e) {
                        logger.warn("Exceptions from tests should not reach this point", e);
                    }
                    Thread.sleep(GlobalArgs.getInstance().getWaitTime());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("InterruptedException(s) should not reach this point", e);
            }
        }
    }

    private class AsyncResultAggregator extends AsyncEngineWorker {
        private List<AsyncTestWorker> testWorkers;

        public AsyncResultAggregator(List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
        }


        private void aggregateResults() {
            for(AsyncTestWorker worker : testWorkers) {
                RunMap localRunMap = worker.getLocalRunMap();
                globalRunMap.aggregateAndReinitialize(localRunMap);
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
                Thread.currentThread().interrupt();
                logger.error("InterruptedException(s) should not reach this point", e);
            }
            aggregateResults();
        }

        public void publishIntermediateResults() {
            for(Publisher publisher : GlobalArgs.getInstance().getPublishers()) {
                publisher.publishIntermediate(globalRunMap.getTestStatistics());
            }
        }
    }

    private class AsyncTimeoutChecker extends AsyncEngineWorker {
        private List<AsyncTestWorker> testWorkers;

        public AsyncTimeoutChecker(TestSuite testSuite, List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
        }

        private void interruptWorkerIfTimeout(AsyncTestWorker worker) {
            if(worker.isTestRunning() && (System.nanoTime() - worker.getLastTestStart()) / 1000000l > GlobalArgs.getInstance().getTimeout()) {
                worker.getWorkerThread().interrupt();
            }
        }

        @Override
        public void run() {
            try {
                while(!finish) {
                    Thread.sleep(Math.round(Math.ceil(GlobalArgs.getInstance().getTimeout() * TIMEOUT_CHECK_FACTOR)));
                    for(AsyncTestWorker worker : testWorkers) {
                        interruptWorkerIfTimeout(worker);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("InterruptedException(s) should not reach this point", e);
            }
        }
    }
}