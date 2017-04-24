package com.adobe.qe.toughday.core.engine.runmodes;

import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.engine.AsyncEngineWorker;
import com.adobe.qe.toughday.core.engine.AsyncTestWorker;
import com.adobe.qe.toughday.core.engine.Engine;
import com.adobe.qe.toughday.core.engine.RunMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Description(desc = "Generates a constant load of test executions, regardless of their execution time.")
public class ConstantLoad implements RunMode {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantLoad.class);

    private static final String DEFAULT_LOAD_STRING = "50";
    private static final int DEFAULT_LOAD = Integer.getInteger(DEFAULT_LOAD_STRING);

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Collection<AsyncTestWorker> testWorkers = Collections.synchronizedSet(new HashSet<AsyncTestWorker>());
    private AsyncTestWorkerScheduler scheduler;
    private ArrayList<RunMap> runMaps;
    private int load = DEFAULT_LOAD;

    @ConfigArgSet(required = false, defaultValue = DEFAULT_LOAD_STRING, desc = "Set the load, in requests per second for the \"constantload\" runmode.")
    public void setLoad(String load) { this.load = Integer.parseInt(load); }

    @ConfigArgGet
    public int getLoad() { return this.load; }

    @Override
    public RunContext runTests(Engine engine) throws Exception {
        this.scheduler = new AsyncTestWorkerScheduler(engine);
        executorService.execute(scheduler);
        runMaps = new ArrayList<>(load);

        for(int i = 0; i < load; i++) {
            runMaps.add(engine.getGlobalRunMap().newInstance());
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
                return scheduler.isFinished();
            }
        };
    }

    @Override
    public void finishExecution() {
        scheduler.finishExecution();
    }

    private class AsyncTestWorkerImpl extends AsyncTestWorker {
        private AbstractTest test;
        private RunMap runMap;

        public AsyncTestWorkerImpl(AbstractTest test, RunMap runMap) {
            this.test = test;
            this.runMap = runMap;
        }

        @Override
        public void run() {
            mutex.lock();
            workerThread = Thread.currentThread();
            currentTest = test;
            mutex.unlock();
            try {
                AbstractTestRunner runner = RunnersContainer.getInstance().getRunner(test);
                runner.runTest(test, runMap);
            } catch (ChildTestFailedException e) {
                LOG.warn("Exceptions from tests should not reach this point", e);
            }
            mutex.lock();
            currentTest = null;
            testWorkers.remove(this);
            Thread.interrupted();
            mutex.unlock();
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
                while (!isFinished()) {
                    ArrayList<AbstractTest> nextRound = new ArrayList<>();
                    long start = System.nanoTime();
                    for (int i = 0; i < load; i++) {
                        AbstractTest nextTest = Engine.getNextTest(engine.getConfiguration().getTestSuite(),
                                engine.getCounts(),
                                engine.getEngineSync());
                        if (null == nextTest) {
                            LOG.info("Constant load scheduler thread finished, because there were no more tests to execute.");
                            this.finishExecution();
                            return;
                        }
                        nextRound.add(nextTest.clone());
                    }

                    try {
                        for (int i = 0; i < load; i++) {
                            AsyncTestWorkerImpl worker = new AsyncTestWorkerImpl(nextRound.get(i), runMaps.get(i));
                            executorService.execute(worker);
                            testWorkers.add(worker);
                        }
                    } catch (OutOfMemoryError e) {
                        LOG.error("The desired load could not be achieved. We could not create enough threads.");
                        finishExecution();
                    }

                    //TODO use this
                    long elapsed = System.nanoTime() - start;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                finishExecution();
                LOG.warn("Constant load scheduler thread was interrupted.");
            }
        }
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
