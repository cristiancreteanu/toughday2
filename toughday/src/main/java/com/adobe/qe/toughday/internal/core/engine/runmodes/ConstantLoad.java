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

import com.adobe.qe.toughday.api.annotations.labels.NotNull;
import com.adobe.qe.toughday.api.annotations.labels.Nullable;
import com.adobe.qe.toughday.api.core.*;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.config.GlobalArgs;
import com.adobe.qe.toughday.internal.core.engine.AsyncEngineWorker;
import com.adobe.qe.toughday.internal.core.engine.AsyncTestWorker;
import com.adobe.qe.toughday.internal.core.engine.Engine;
import com.adobe.qe.toughday.internal.core.engine.RunMode;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Description(desc = "Generates a constant load of test executions, regardless of their execution time.")
public class ConstantLoad implements RunMode {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantLoad.class);

    private static final String DEFAULT_LOAD_STRING = "50";
    private static final int DEFAULT_LOAD = Integer.parseInt(DEFAULT_LOAD_STRING);
    private static final String DEFAULT_INTERVAL_STRING = "1s";
    private static final long DEFAULT_INTERVAL = 1000;

    private AtomicBoolean loggedWarning = new AtomicBoolean(false);

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Collection<AsyncTestWorker> testWorkers = Collections.synchronizedSet(new HashSet<AsyncTestWorker>());
    private AsyncTestWorkerScheduler scheduler;
    private final List<RunMap> runMaps = new ArrayList<>();
    private int load = DEFAULT_LOAD;
    private int start = DEFAULT_LOAD;
    private int end = DEFAULT_LOAD;
    private long interval = DEFAULT_INTERVAL;
    private int rate;
    private int currentLoad;

    private TestCache testCache;

    @ConfigArgSet(required = false, defaultValue = DEFAULT_LOAD_STRING, desc = "Set the load, in requests per second for the \"constantload\" runmode.")
    public void setLoad(String load) { this.load = Integer.parseInt(load); }

    @ConfigArgGet
    public int getLoad() { return this.load; }

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
    public void setInterval(String interval) {
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

    private static class TestCache {
        public Map<TestId, Queue<AbstractTest>> cache = new HashMap<>();

        public TestCache(TestSuite testSuite) {
            for(AbstractTest test : testSuite.getTests()) {
                cache.put(test.getId(), new ConcurrentLinkedQueue());
            }
        }

        public void add(@NotNull AbstractTest test) {
            cache.get(test.getId()).add(test);
        }

        public @Nullable AbstractTest getCachedValue(@NotNull TestId testID) {
            return cache.get(testID).poll();
        }
    }

    @Override
    public void runTests(Engine engine) throws Exception {
        Configuration configuration = engine.getConfiguration();
        TestSuite testSuite = configuration.getTestSuite();
        this.testCache = new TestCache(testSuite);

        if (start != -1 && end != -1) {
            load = start > end? start : end;
        }

        for(int i = 0; i < load; i++) {
            synchronized (runMaps) {
                runMaps.add(engine.getGlobalRunMap().newInstance());
            }
        }

        this.scheduler = new AsyncTestWorkerScheduler(engine);
        executorService.execute(scheduler);
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
                return scheduler.isFinished();
            }
        };
    }

    @Override
    public void finishExecutionAndAwait() {
        scheduler.finishExecution();

        synchronized (testWorkers) {
            for(AsyncTestWorker testWorker : testWorkers) {
                testWorker.finishExecution();
            }
        }

        boolean allExited = false;
        while(!allExited) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            allExited = true;

            synchronized (testWorkers) {
                for (AsyncTestWorker testWorker : testWorkers) {
                    if (!testWorker.hasExited()) {
                        if(!testWorker.getMutex().tryLock())
                            continue;
                        allExited = false;
                        testWorker.getWorkerThread().interrupt();
                        testWorker.getMutex().unlock();
                    }
                }
            }
        }

    }

    private class AsyncTestWorkerImpl extends AsyncTestWorker {
        private AbstractTest test;
        private RunMap runMap;
        private boolean exited = false;

        public AsyncTestWorkerImpl(AbstractTest test, RunMap runMap) {
            this.test = test;
            this.runMap = runMap;
        }

        @Override
        public void run() {
            mutex.lock();
            lastTestStart = System.nanoTime();
            workerThread = Thread.currentThread();
            currentTest = test;
            mutex.unlock();
            try {
                AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(test);
                runner.runTest(test, runMap);
            } catch (Throwable e) {
                LOG.warn("Exceptions from tests should not reach this point", e);
            }

            mutex.lock();
            currentTest = null;
            exited = true;
            testCache.add(test);
            Thread.interrupted();
            mutex.unlock();
        }

        @Override
        public boolean hasExited() {
            return exited;
        }
    }


    private class AsyncTestWorkerScheduler extends AsyncEngineWorker {
        private Engine engine;
        public AsyncTestWorkerScheduler(Engine engine) {
            this.engine = engine;
        }

        @Override
        public void run() {
            try {
                currentLoad = start;
                MutableLong secondsUntilLoadIncreaseOrDecrease = new MutableLong(interval);

                //the difference from the beginning load to the end one
                int loadDifference = start > end? start - end : end - start;

                // if the rate was not specified and either start or end were
                if (rate == -1 && start != -1 && end != -1) {
                    // suppose load will increase by second
                    secondsUntilLoadIncreaseOrDecrease.setValue(1);
                    rate = (int)Math.floor(1.0 * secondsUntilLoadIncreaseOrDecrease.getValue() * loadDifference
                            / engine.getGlobalArgs().getDuration());

                    // if the rate becomes too small, increase the interval at which the load is increased
                    while (rate < 1) {
                        secondsUntilLoadIncreaseOrDecrease.increment();
                        rate = (int)Math.floor(1.0 * secondsUntilLoadIncreaseOrDecrease.getValue() * loadDifference
                                / engine.getGlobalArgs().getDuration());
                    }

                    interval = secondsUntilLoadIncreaseOrDecrease.getValue();
                }

                while (!isFinished()) {
                    // run the current run with the current load
                    runRound();

                    secondsUntilLoadIncreaseOrDecrease.decrement();

                    // ramp up the load if 'start' was specified
                    rampUp(secondsUntilLoadIncreaseOrDecrease);

                    // ramp down the load if 'end' was specified
                    rampDown(secondsUntilLoadIncreaseOrDecrease);
                }
            } catch (InterruptedException e) {
                finishExecution();
                LOG.warn("Constant load scheduler thread was interrupted.");
            }
        }

        private void rampUp(MutableLong secondsUntilLoadIncreaseOrDecrease) {
            if (secondsUntilLoadIncreaseOrDecrease.getValue() == 0 && currentLoad < end) {
                secondsUntilLoadIncreaseOrDecrease.setValue(interval);
                currentLoad += rate;

                if (currentLoad > end) {
                    currentLoad = end;
                }
            }
        }

        private void rampDown(MutableLong secondsUntilLoadIncreaseOrDecrease) {
            if (secondsUntilLoadIncreaseOrDecrease.getValue() == 0 && currentLoad > end) {
                secondsUntilLoadIncreaseOrDecrease.setValue(interval);
                currentLoad -= rate;

                if (currentLoad < end) {
                    currentLoad = end;
                }
            }
        }

        private void runRound() throws InterruptedException {
            ArrayList<AbstractTest> nextRound = new ArrayList<>();
            for (int i = 0; i < currentLoad; i++) {
                AbstractTest nextTest = Engine.getNextTest(engine.getConfiguration().getTestSuite(),
                        engine.getCounts(),
                        engine.getEngineSync());
                if (null == nextTest) {
                    LOG.info("Constant load scheduler thread finished, because there were no more tests to execute.");
                    this.finishExecution();
                    return;
                }

                //Use a cache test if available
                AbstractTest localNextTest = testCache.getCachedValue(nextTest.getId());
                if(localNextTest == null) {
                    localNextTest = nextTest.clone();
                }

                nextRound.add(localNextTest);
            }

            for (int i = 0; i < currentLoad && !isFinished(); i++) {
                AsyncTestWorkerImpl worker = new AsyncTestWorkerImpl(nextRound.get(i), runMaps.get(i));
                try {
                    executorService.execute(worker);
                } catch (OutOfMemoryError e) {
                    if (!loggedWarning.getAndSet(true)) {
                        LOG.warn("The desired load could not be achieved. We are creating as many threads as possible.");
                    }
                    break;
                }
                synchronized (testWorkers) {
                    testWorkers.add(worker);
                }
            }

            //TODO use this
            Thread.sleep(1000);
        }
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
