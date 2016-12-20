package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.config.Configuration;
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
 * Engine for running a test suite.
 */
public class Engine {
    protected static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    protected static final int RESULT_AGGREATION_DELAY = 1000; //in ms
    protected static final int WAIT_TERMINATION_FACTOR = 30;
    protected static final double TIMEOUT_CHECK_FACTOR = 0.03;
    protected static Random _rnd = new Random();

    private TestSuite testSuite;
    private Configuration.GlobalArgs globalArgs;
    private ExecutorService testsExecutorService;
    private ExecutorService engineExecutorService;
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
        this.testsExecutorService = Executors.newFixedThreadPool(globalArgs.getConcurrency());
        this.engineExecutorService = Executors.newFixedThreadPool(2);

        this.globalRunMap = new RunMap(globalArgs.getConcurrency());
        for(AbstractTest test : testSuite.getTests()) {
            add(test);
        }
    }

    /**
     * Returns the Global Run map object
     * @return
     */
    protected RunMap getGlobalRunMap() {
        return globalRunMap;
    }

    /**
     * Returns the global args
     * @return
     */
    protected Configuration.GlobalArgs getGlobalArgs() {
        return globalArgs;
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
        createRunners(test);
        addToRunMap(test);
        return this;
    }

    private Engine addToRunMap(AbstractTest test) {
        globalRunMap.addTest(test);
        if(test.includeChildren()) {
            for (AbstractTest child : test.getChildren()) {
                addToRunMap(child);
            }
        }
        return this;
    }

    private Engine createRunners(AbstractTest test)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunnersContainer.getInstance().addRunner(test);
        for(AbstractTest child : test.getChildren()) {
            createRunners(child);
        }
        return this;
    }

    /**
     * Method for starting running tests.
     * @throws Exception
     */
    public void runTests() {
        try {
            run();
        } catch (Exception e) {
            LOG.error("Failure in tests execution ", e);
        }
    }

    private void run() throws Exception {
        // Run the setup step of the suite
        if (testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }
        // Create the test worker threads
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for (int i = 0; i < globalArgs.getConcurrency(); i++) {
            AsyncTestWorker runner = new AsyncTestWorker(this, testSuite, globalRunMap.newInstance());
            testWorkers.add(runner);
            testsExecutorService.execute(runner);
        }

        // Create the result aggregator thread
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(this, testWorkers);
        engineExecutorService.execute(resultAggregator);

        // create the timeout chekcer thread
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(this, testSuite, testWorkers, Thread.currentThread());
        engineExecutorService.execute(timeoutChecker);

        // This thread sleeps until the duration
        try {
            Thread.sleep(globalArgs.getDuration() * 1000L);
        } catch (InterruptedException e) {
            LOG.info("Engine Interrupted", e);
        }
        // Then close all threads
        finally {
            for (AsyncTestWorker run : testWorkers) {
                run.finishExecution();
            }
            resultAggregator.finishExecution();
            timeoutChecker.finishExecution();
        }
        // interrupt extra test threads
        // TODO: this is suboptimal, replace with a better mechanism for notifications
        List<Thread> threadsList = AbstractTest.getExtraThreads();
        synchronized (threadsList) {
            for (Thread t : threadsList) {
                t.interrupt();
            }
        }

        shutdownAndAwaitTermination(testsExecutorService);
        shutdownAndAwaitTermination(engineExecutorService);
        publishFinalResults();
    }

    /**
     * Publish final results.
     */
    private void publishFinalResults() {
        for (Publisher publisher : globalArgs.getPublishers()) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }


    /**
     * Method for getting the next weighted random test form the test suite
     * TODO: optimize
     */
    protected static AbstractTest getNextTest(TestSuite testSuite, RunMap globalRunMap) {
        int randomNumber = _rnd.nextInt(testSuite.getTotalWeight());
        AbstractTest selectedTest = null;
        for (AbstractTest test : testSuite.getTests()) {
            int testWeight = testSuite.getWeightMap().get(test);
            boolean selectTest = (randomNumber < testWeight);

            // Approximation based on how many times the test has been run globally
            long totalRuns = globalRunMap.getRecord(test).getTotalRuns();
            if (null != testSuite.getCount(test)) {
                selectTest &= totalRuns < testSuite.getCount(test);
            }
            if (selectTest) {
                selectedTest = test;
                break;
            }
            randomNumber = randomNumber - testWeight;
        }

        return selectedTest;
    }

    /**
     * Method for forcing an ExecutorService to finish.
     * @param pool
     */
    protected void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.MILLISECONDS))
                    LOG.error("Thread pool did not terminate. Process must be killed");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
