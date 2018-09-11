/*
Copyright 2015 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/
package com.adobe.qe.toughday.internal.core.engine.runmodes;

import com.adobe.qe.toughday.api.core.*;
import com.adobe.qe.toughday.internal.core.*;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.config.GlobalArgs;
import com.adobe.qe.toughday.internal.core.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

@Description(desc = "Runs tests normally.")
public class Normal implements RunMode {

    private static final Logger LOG = LoggerFactory.getLogger(Normal.class);

    private static final String DEFAULT_CONCURRENCY_STRING = "200";
    private static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_STRING);

    private static final String DEFAULT_WAIT_TIME_STRING = "300";
    private static final long DEFAULT_WAIT_TIME = Long.parseLong(DEFAULT_WAIT_TIME_STRING);

    private static final String DEFAULT_INTERVAL_STRING = "1s";
    private static final long DEFAULT_INTERVAL = 1000;

    private ExecutorService testsExecutorService;

    private final List<AsyncTestWorker> testWorkers = Collections.synchronizedList(new LinkedList<>());
    private final List<AsyncTestWorker> testWorkersToRemove = Collections.synchronizedList(new ArrayList<>());
    private final List<RunMap> runMaps = new ArrayList<>();

    private int start = DEFAULT_CONCURRENCY;
    private int end = DEFAULT_CONCURRENCY;
    private int concurrency = DEFAULT_CONCURRENCY;
    private int rate;
    private long waitTime = DEFAULT_WAIT_TIME;
    private long interval = DEFAULT_INTERVAL;
    private int activeThreads = 0;

    @ConfigArgGet
    public int getConcurrency() {
        return concurrency;
    }

    @ConfigArgSet(required = false, desc = "The number of concurrent threads that Tough Day will use",
            defaultValue = DEFAULT_CONCURRENCY_STRING, order = 5)
    public void setConcurrency(String concurrencyString) {
        this.concurrency = Integer.parseInt(concurrencyString);
    }

    @ConfigArgGet
    public long getWaitTime() {
        return waitTime;
    }

    @ConfigArgSet(required = false, desc = "The wait time between two consecutive test runs for a specific thread. Expressed in milliseconds",
            defaultValue = DEFAULT_WAIT_TIME_STRING, order = 7)
    public void setWaitTime(String waitTime) {
        this.waitTime = Integer.parseInt(waitTime);
    }

    @ConfigArgGet
    public int getStart() {
        return start;
    }

    @ConfigArgSet(required = false, desc = "The number of threads to start ramping up from. Will rise to the number specified by \"concurrency\".",
            defaultValue = "-1")
    public void setStart(String start) {
        this.start = Integer.valueOf(start);
    }

    @ConfigArgGet
    public int getRate() {
        return rate;
    }

    @ConfigArgSet(required = false, desc = "The number of users added per time unit. When it equals -1, it means it is not set.", defaultValue = "-1")
    public void setRate(String rate) {
        this.rate = Integer.valueOf(rate);
    }

    @ConfigArgGet
    public long getInterval() {
        return interval;
    }

    @ConfigArgSet(required = false, desc = "Used with rate to specify the time interval to add threads.", defaultValue = DEFAULT_INTERVAL_STRING)
    public void setTimeUnit(String interval) {
        this.interval = GlobalArgs.parseDurationToSeconds(interval);
    }

    @ConfigArgGet
    public int getEnd() {
        return end;
    }

    @ConfigArgSet(required = false, desc = "The number of threads to keep running.", defaultValue = "-1")
    public void setEnd(String end) {
        this.end = Integer.valueOf(end);
    }

    @Override
    public void runTests(Engine engine) throws Exception {
        Configuration configuration = engine.getConfiguration();
        TestSuite testSuite = configuration.getTestSuite();
        GlobalArgs globalArgs = configuration.getGlobalArgs();
        testsExecutorService = Executors.newFixedThreadPool(concurrency);

        // if no rate was provided, we'll create/remove one user at fixed rate,
        // namely every 'interval' milliseconds
        if (start != -1 && end != -1) {
            if (rate == -1) {
                interval = (long)Math.floor(1000.0 * globalArgs.getDuration()
                        / (start < end? end - start : start - end));  // to replace with phase duration
                rate = 1;
            }
            concurrency = start;
        }

        // Create the test worker threads
        // if start was provided, then it will create 'start' workers to begin with
        // otherwise, start == concurrency, so it will create 'concurrency' workers
        for (int i = 0; i < concurrency; i++) {
            addWorkerToThreadPool(testsExecutorService, engine, testSuite);
        }

        rampUp(engine, testSuite);
        rampDown();
    }

    private void rampUp(Engine engine, TestSuite testSuite) {
        // every 'interval' milliseconds, we'll create 'rate' workers
        if (start < end) {
            ScheduledExecutorService addWorkerScheduler = Executors.newSingleThreadScheduledExecutor();
            addWorkerScheduler.scheduleAtFixedRate(() -> {
                for (int i = 0; i < rate; ++i) {
                    if (activeThreads >= end) {
                        addWorkerScheduler.shutdownNow();
                    } else {
                        addWorkerToThreadPool(testsExecutorService, engine, testSuite);
                    }
                }
            }, 0, interval, TimeUnit.MILLISECONDS);
        }
    }

    private void rampDown() throws InterruptedException {
        // if the 'end' was specified by the user
        if (end < start) {  //////////////////trebuie sa fac end si start sa fie egale cu concurrency cand nu sunt specificate
//            Thread.sleep(1000);
            ScheduledExecutorService removeWorkerScheduler = Executors.newSingleThreadScheduledExecutor();
            long s = System.currentTimeMillis();
            ThreadPoolExecutor executor = (ThreadPoolExecutor)testsExecutorService;

//            threadsToStop = (start - testWorkersToRemove.size() < end ? start - end : testWorkersToRemove.size());
//            toRemove = rate;
//
//            ListIterator<AsyncTestWorker> testWorkerIterator = testWorkersToRemove.listIterator(testWorkersToRemove.size());
//            while (testWorkerIterator.hasPrevious() && threadsToStop > 0) {
//                AsyncTestWorker testWorker = testWorkerIterator.previous();
//
//                testWorker.getWorkerThread().interrupt();
//                testWorkerIterator.remove();
//                --threadsToStop;
//                --toRemove;
//                --activeThreads;
//
//                System.out.println(activeThreads);
//
//                if (toRemove == 0) {
//                    Thread.sleep(interval);
//                    toRemove = rate;
//                }
//            }
//
//            executor.setCorePoolSize(executor.getCorePoolSize() - threadsToStop);
//            executor.setMaximumPoolSize(executor.getCorePoolSize());

            // if all the idle threads were interrupted, but there are still active threads
            // that need to be interrupted
//            if (executor.getCorePoolSize() != end) {
            removeWorkerScheduler.scheduleAtFixedRate(() -> {
                Iterator<AsyncTestWorker> testWorkerIterator = testWorkers.iterator();
                int toRemove = rate;

                while (testWorkerIterator.hasNext()) {
                    if (activeThreads <= end) {
                        removeWorkerScheduler.shutdownNow();
                    } else {
                        AsyncTestWorker testWorker = testWorkerIterator.next();

                        // hmmmmm.. i wonder
                        if (testWorker.isFinished()) {
                            continue;
                        }

                        testWorker.getWorkerThread().interrupt();
                        testWorkerIterator.remove();
                        --toRemove;
                        --activeThreads;

                        System.out.println(activeThreads);

                        if (toRemove == 0) {
                            executor.setCorePoolSize(executor.getCorePoolSize() - rate);
                            executor.setMaximumPoolSize(executor.getCorePoolSize());
                            break;
                        }
                    }
                }
            }, 0, interval, TimeUnit.MILLISECONDS);
//            }

            System.out.println(System.currentTimeMillis() - s);
        }
    }

    private boolean addWorkerToThreadPool(ExecutorService testsExecutorService, Engine engine, TestSuite testSuite) {
        AsyncTestWorkerImpl testWorker = new AsyncTestWorkerImpl(engine, testSuite, engine.getGlobalRunMap().newInstance());
        try {
            testsExecutorService.execute(testWorker);
        } catch (OutOfMemoryError e) {
            LOG.warn("Could not create the required number of threads. Number of created threads : " + String.valueOf(activeThreads) + ".");
            return false;
        }
        synchronized (testWorkers) {
            testWorkers.add(testWorker);
            activeThreads++;
        }
        synchronized (runMaps) {
            runMaps.add(testWorker.getLocalRunMap());
        }

        return true;
    }

    public RunContext getRunContext() {
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
    public void finishExecutionAndAwait() {
        long start = System.currentTimeMillis();
        synchronized (testWorkers) {
            for (AsyncTestWorker testWorker : testWorkers) {
                testWorker.finishExecution();
            }
        }

        boolean allExited = false;
        while(!allExited) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            allExited = true;
            synchronized (testWorkers) {
                for (AsyncTestWorker testWorker : testWorkers) {
                    if (!testWorker.hasExited()) {
                        if(!testWorker.getMutex().tryLock()) {
                            continue;
                        }
                        allExited = false;

                        testWorker.getWorkerThread().interrupt();
                        testWorker.getMutex().unlock();
                    }
                }
            }
        }

//        System.out.println((System.currentTimeMillis() - start));

    }

    @Override
    public ExecutorService getExecutorService() {
        return testsExecutorService;
    }

    private class AsyncTestWorkerImpl extends AsyncTestWorker {
        protected final Engine engine;
        private HashMap<TestId, AbstractTest> localTests;
        private TestSuite testSuite;
        private RunMap localRunMap;
        private boolean exited = false;

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
                        testWorkersToRemove.add(this);

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
                    } catch (Throwable e) {
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
                this.exited = true;
            }
        }

        public RunMap getLocalRunMap() {
            return localRunMap;
        }

        @Override
        public boolean hasExited() {
            return exited;
        }
    }
 }
