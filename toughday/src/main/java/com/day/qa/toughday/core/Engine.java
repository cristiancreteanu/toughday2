package com.day.qa.toughday.core;

import com.day.qa.toughday.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tuicu on 17/09/15.
 * Engine for running a test suite.
 */
public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    private static final int RESULT_AGGREATION_DELAY = 1000; //in ms
    private static final int WAIT_TERMINATION_FACTOR = 3;
    private static final double TIMEOUT_CHECK_FACTOR = 0.3;
    private static Random _rnd = new Random();

    /**
     * Method for getting the next weighted random test form the test suite
     */
    private static AbstractTest getNextTest(TestSuite testSuite) {
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

    /**
     * Method for forcing a ExecutorService to finnish.
     * @param pool
     */
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
    private Configuration.GlobalArgs globalArgs;
    private ExecutorService executorService;
    private RunMap globalRunMap;

    /**
     * Constructor
     * @param configuration A Configuration object.
     * @throws InvocationTargetException caused by reflection
     * @throws NoSuchMethodException caused by reflection
     * @throws InstantiationException caused by reflection
     * @throws IllegalAccessException caused by reflection
     */
    public Engine(Configuration configuration)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.testSuite = configuration.getTestSuite();
        this.globalArgs = configuration.getGlobalArgs();
        this.executorService = Executors.newFixedThreadPool(globalArgs.getConcurrency() + 2);
        this.globalRunMap = new RunMap(globalArgs.getConcurrency());
        for(AbstractTest test : testSuite.getTests()) {
            add(test);
        }
    }

    /**
     * Recursive method for preparing a test to run.
     * @param test
     * @return this object. (builder pattern)
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private Engine add(AbstractTest test)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        globalRunMap.addTest(test);
        RunnersContainer.getInstance().addRunner(test);
        for(AbstractTest child : test.getChildren()) {
            add(child);
        }
        return this;
    }

    /**
     * Method for starting running tests.
     * @throws Exception
     */
    public void runTests() throws Exception {
        if(testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for(int i = 0; i < globalArgs.getConcurrency(); i++) {
            AsyncTestWorker runner = new AsyncTestWorker(testSuite, globalRunMap.newInstance());
            testWorkers.add(runner);
            executorService.execute(runner);
        }
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(testWorkers);
        executorService.execute(resultAggregator);
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(testSuite, testWorkers);
        executorService.execute(timeoutChecker);
        try {
            Thread.sleep(globalArgs.getDuration() * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            for(AsyncTestWorker run : testWorkers)
                run.finishExecution();
            resultAggregator.finishExecution();
            timeoutChecker.finishExecution();
        }
        shutdownAndAwaitTermination(executorService);
        publishFinalResults();
    }

    /**
     * Publish final results.
     */
    private void publishFinalResults() {
        for(Publisher publisher : globalArgs.getPublishers()) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }

    /**
     * Base class for all async workers in engine.
     */
    private abstract class AsyncEngineWorker implements Runnable {
        private boolean finish = false;

        /**
         * Method for correctly shutting down a worker.
         */
        public void finishExecution() {
            finish = true;
        }

        /**
         * Method for checking if a worker has finished.
         */
        public boolean isFinished() { return finish; }
    }

    /**
     * Async worker for running tests. There will be GlobalArgs.Concurrency test workers.
     */
    private class AsyncTestWorker extends AsyncEngineWorker {
        private RunMap localRunMap;
        private List<AbstractTest> localTests;
        private Thread workerThread;
        private long lastTestStart;
        private TestSuite testSuite;
        private AbstractTest currentTest;
        private ReentrantLock mutex;

        /**
         * Constructor
         * @param testSuite the test suite
         * @param localRunMap a deep clone a the global run map.
         */
        public AsyncTestWorker(TestSuite testSuite, RunMap localRunMap) {
            this.mutex = new ReentrantLock();
            this.testSuite = testSuite;
            localTests = new ArrayList<>();
            for(AbstractTest test : testSuite.getTests()) {
                AbstractTest localTest = test.clone();
                localTests.add(localTest);
            }
            this.localRunMap = localRunMap;
        }

        /**
         * Getter for the thread running this worker.
         * @return thread running this worker, if the worker has started running, null otherwise.
         */
        public Thread getWorkerThread() {
            return workerThread;
        }

        /**
         * Getter for the nanoTime of the last time a test has started running.
         */
        public long getLastTestStart() {
            return lastTestStart;
        }

        /**
         * Getter for the local run map.
         */
        public RunMap getLocalRunMap() {
            return localRunMap;
        }

        /**
         * Getter for the current running test.
         * @return the running test, or null if no test has started running.
         */
        public AbstractTest getCurrentTest() { return currentTest; }

        /**
         * Getter for the mutex that is only unlocked when the test is running.
         */
        public ReentrantLock getMutex() { return mutex; }

        /**
         * Method for running tests.
         */
        @Override
        public void run() {
            workerThread = Thread.currentThread();
            logger.info("Thread running: " + workerThread);
            mutex.lock();
            try {
                while (!isFinished()) {
                    currentTest = getNextTest(this.testSuite);
                    AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(currentTest);

                    lastTestStart = System.nanoTime();
                    mutex.unlock();
                    try {
                        runner.runTest(currentTest, localRunMap);
                    } catch (ChildTestFailedException e) {
                        logger.warn("Exceptions from tests should not reach this point", e);
                    }
                    mutex.lock();
                    Thread.interrupted();
                    Thread.sleep(globalArgs.getWaitTime());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("InterruptedException(s) should not reach this point", e);
            } finally {
                mutex.unlock();
            }
        }
    }

    /**
     * Worker for aggregating and publishing benchmarks.
     */
    private class AsyncResultAggregator extends AsyncEngineWorker {
        private List<AsyncTestWorker> testWorkers;

        /**
         * Constructor.
         * @param testWorkers list of test workers from this engine.
         */
        public AsyncResultAggregator(List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
        }

        /**
         * Method aggregating results.
         */
        private void aggregateResults() {
            for(AsyncTestWorker worker : testWorkers) {
                RunMap localRunMap = worker.getLocalRunMap();
                globalRunMap.aggregateAndReinitialize(localRunMap);
            }
        }

        /**
         * Implementation of the Runnable interface.
         */
        @Override
        public void run() {
            try {
                while(!isFinished()) {
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

        /**
         * Method for publishing intermediate results.
         */
        public void publishIntermediateResults() {
            for(Publisher publisher : globalArgs.getPublishers()) {
                publisher.publishIntermediate(globalRunMap.getTestStatistics());
            }
        }
    }

    /**
     * Worker for checking timeout and interrupting test worker threads when timeout is exceeded.
     * It uses Thread.interrupt for letting workers know the
     */
    private class AsyncTimeoutChecker extends AsyncEngineWorker {
        private List<AsyncTestWorker> testWorkers;
        private long minTimeout;
        private TestSuite testSuite;

        /**
         * Constructor.
         * @param testSuite
         * @param testWorkers list of test workers from this engine.
         */
        public AsyncTimeoutChecker(TestSuite testSuite, List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
            this.testSuite = testSuite;
            minTimeout = globalArgs.getTimeout();
            for(AbstractTest test : testSuite.getTests()) {
                if(testSuite.getTimeout(test) == null) {
                    continue;
                }
                minTimeout = Math.min(minTimeout, testSuite.getTimeout(test));
            }
        }

        /**
         * Method for checking timeout and interrupting test worker threads if timeout is exceeded.
         * It uses Thread.interrupt for letting worker threads know the timeout has exceeded. Runners must know
         * how to correctly handled all outcomes of a Thread.interrupt see:
         * http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#interrupt()
         */
        private void interruptWorkerIfTimeout(AsyncTestWorker worker) {
            AbstractTest currentTest = worker.getCurrentTest();
            if(currentTest == null)
                return;

            Long testTimeout = testSuite.getTimeout(currentTest);
            long timeout = testTimeout != null ? testTimeout : globalArgs.getTimeout();

            if(!worker.getMutex().tryLock()) {
                /* nothing to interrupt. if the test was running
                   the mutex would've been successfully acquired. */
                return;
            }

            try {
                if (((System.nanoTime() - worker.getLastTestStart()) / 1000000l > timeout)
                        && currentTest == worker.getCurrentTest()) {
                    worker.getWorkerThread().interrupt();
                }
            } finally {
                worker.getMutex().unlock();
            }
        }

        /**
         * Implementation of Runnable interface.
         */
        @Override
        public void run() {
            try {
                while(!isFinished()) {
                    Thread.sleep(Math.round(Math.ceil(minTimeout * TIMEOUT_CHECK_FACTOR)));
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
