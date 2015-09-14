package com.day.qa.toughday.core;

import com.day.qa.toughday.core.cli.CliArg;
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
    private SuiteSetup setupStep;
    private int timeout;

    HashMap<AbstractTest, Integer> weightMap;

    public TestSuite() {
        this.globalTestList = new ArrayList<>();
        this.weightMap = new HashMap<>();
        this.publishers = new ArrayList<>();
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
        RunnersContainer.getInstance().addRunner(test);
        for(AbstractTest child : test.getChildren()) {
            add(child);
        }
        return this;
    }

    private void add(AbstractTest child)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        for(AbstractTest c : child.getChildren()) {
            add(c);
        }
        /* Children are added to the runners container and the run map only.
           They must not be added to the test list or the weight map, because they will
           not be ran outside of their composite test */
        RunnersContainer.getInstance().addRunner(child);
        globalRunMap.addTest(child);
    }

    public TestSuite addPublisher(Publisher publisher) {
        publishers.add(publisher);
        return this;
    }
    
    public void runTests() throws Exception {
        if(setupStep != null) {
            setupStep.setup();
        }
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for(int i = 0; i < concurrency; i++) {
            AsyncTestWorker runner = new AsyncTestWorker(globalRunMap.newInstance());
            testWorkers.add(runner);
            executorService.execute(runner);
        }
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(testWorkers);
        executorService.execute(resultAggregator);
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(testWorkers);
        //executorService.execute(timeoutChecker);
        try {
            Thread.sleep(duration * 1000);
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
        for(Publisher publisher : publishers) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }

    private abstract class AsyncTestSuiteWorker implements Runnable {
        protected boolean finish = false;

        public void finishExecution() {
            finish = true;
        }
    }

    private class AsyncTestWorker extends AsyncTestSuiteWorker {
        private RunMap localRunMap;
        private List<AbstractTest> localTests;
        private Thread workerThread;
        private long lastTestStart;
        private boolean testRunning;

        public AsyncTestWorker(RunMap localRunMap) {
            localTests = new ArrayList<>();
            for(AbstractTest test : globalTestList) {
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
                    AbstractTest nextTest = getNextTest(localTests, totalWeight);
                    AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(nextTest);

                    try {
                        lastTestStart = System.nanoTime();
                        testRunning = true;
                        runner.runTest(nextTest, localRunMap);
                        testRunning = false;
                    } catch (ChildTestFailedException e) {
                        logger.warn("Exceptions from tests should not reach this point", e);
                    }
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class AsyncResultAggregator extends AsyncTestSuiteWorker {
        private List<AsyncTestWorker> testWorkers;

        public AsyncResultAggregator(List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
        }


        private void aggregateResults() {
            for(AsyncTestWorker worker : testWorkers) {
                RunMap localRunMap = worker.getLocalRunMap();
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

    private class AsyncTimeoutChecker extends AsyncTestSuiteWorker {
        private List<AsyncTestWorker> testWorkers;

        public AsyncTimeoutChecker(List<AsyncTestWorker> testWorkers) {
            this.testWorkers = testWorkers;
        }
        @Override
        public void run() {
            try {
                while(!finish) {
                    Thread.sleep(timeout / 2);
                    for(AsyncTestWorker worker : testWorkers) {
                        if(worker.isTestRunning() && (System.nanoTime() - worker.getLastTestStart()) / 1000000000l > timeout) {
                            worker.getWorkerThread().interrupt();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
