package com.adobe.qe.toughday.core.engine.runmodes;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.core.engine.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Description(desc = "Runs tests normally.")
public class Normal implements RunMode {

    public static final String DEFAULT_CONCURRENCY_STRING = "200";
    public static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_STRING);

    public static final String DEFAULT_WAIT_TIME_STRING = "300";
    public static final long DEFAULT_WAIT_TIME = Long.parseLong(DEFAULT_WAIT_TIME_STRING);

    private ExecutorService testsExecutorService;
    private List<AsyncTestWorker> testWorkers = new ArrayList<>();
    private List<RunMap> runMaps = new ArrayList<RunMap>();

    private int concurrency = DEFAULT_CONCURRENCY;
    private long waitTime = DEFAULT_WAIT_TIME;

    @ConfigArgSet(required = false, desc = "The number of concurrent threads that Tough Day will use", defaultValue = DEFAULT_CONCURRENCY_STRING, order = 5)
    public void setConcurrency(String concurrencyString) {
        this.concurrency = Integer.parseInt(concurrencyString);
    }

    @ConfigArgSet(required = false, desc = "The wait time between two consecutive test runs for a specific thread. Expressed in milliseconds",
            defaultValue = DEFAULT_WAIT_TIME_STRING, order = 7)
    public void setWaitTime(String waitTime) {
        this.waitTime = Integer.parseInt(waitTime);
    }

    @ConfigArgGet
    public int getConcurrency() {
        return concurrency;
    }

    @ConfigArgGet
    public long getWaitTime() {
        return waitTime;
    }

    @Override
    public RunContext runTests(Engine engine) throws Exception {
        Configuration configuration = engine.getConfiguration();
        TestSuite testSuite = configuration.getTestSuite();
        Configuration.GlobalArgs globalArgs = configuration.getGlobalArgs();
        testsExecutorService = Executors.newFixedThreadPool(concurrency);

        // Create the test worker threads
        for (int i = 0; i < concurrency; i++) {
            AsyncTestWorkerImpl testWorker = new AsyncTestWorkerImpl(engine, testSuite, engine.getGlobalRunMap().newInstance());
            testWorkers.add(testWorker);
            runMaps.add(testWorker.getLocalRunMap());
            testsExecutorService.execute(testWorker);
        }

        return new RunContext() {
            @Override
            public Collection<AsyncTestWorker> getTestWorkers() {
                return testWorkers;
            }

            @Override
            public Collection<RunMap> getRunMaps() {
                return runMaps;
            }

            @Override
            public boolean isRunFinished() {
                for(AsyncTestWorker testWorker : testWorkers) {
                    if (!testWorker.isFinished())
                        return false;
                }
                return true;
            }
        };
    }

    @Override
    public void finishExecution() {
        for (AsyncTestWorker run : testWorkers) {
            run.finishExecution();
        }
    }

    @Override
    public ExecutorService getExecutorService() {
        return testsExecutorService;
    }

    private class AsyncTestWorkerImpl extends AsyncTestWorker {
        protected final Engine engine;
        private HashMap<UUID, AbstractTest> localTests;
        private TestSuite testSuite;
        private RunMap localRunMap;

        /**
         * Constructor
         *
         * @param engine
         * @param testSuite   the test suite
         * @param localRunMap a deep clone a the global run map.
         */
        public AsyncTestWorkerImpl(Engine engine, TestSuite testSuite, RunMap localRunMap) {
            this.engine = engine;
            this.testSuite = testSuite;
            localTests = new HashMap<>();
            for(AbstractTest test : testSuite.getTests()) {
                AbstractTest localTest = test.clone();
                localTests.put(localTest.getId(), localTest);
            }
            this.localRunMap = localRunMap;
        }

        /**
         * Method for running tests.
         */
        @Override
        public void run() {
            workerThread = Thread.currentThread();
            LOG.debug("Thread running: " + workerThread);
            mutex.lock();
            try {
                while(!isFinished()) {
                    currentTest = Engine.getNextTest(this.testSuite, engine.getCounts(), engine.getEngineSync());
                    // if no test available, finish
                    if (null == currentTest) {
                        LOG.info("Thread " + workerThread + " died! :(");
                        this.finishExecution();
                        continue;
                    }

                    //get the worker's local test to run
                    currentTest = localTests.get(currentTest.getId());

                    // else, continue with the run

                    AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(currentTest);

                    lastTestStart = System.nanoTime();
                    mutex.unlock();
                    try {
                        runner.runTest(currentTest, localRunMap);
                    } catch (ChildTestFailedException e) {
                        LOG.warn("Exceptions from tests should not reach this point", e);
                    }
                    mutex.lock();
                    Thread.interrupted();
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("InterruptedException(s) should not reach this point", e);
            } catch (Throwable e) {
                LOG.error("Unexpected exception caught", e);
            } finally {
                mutex.unlock();
            }
        }

        public RunMap getLocalRunMap() {
            return localRunMap;
        }
    }
 }
