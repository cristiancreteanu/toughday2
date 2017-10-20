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
import sun.rmi.runtime.Log;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Description(desc = "Generates a constant load of test executions, regardless of their execution time.")
public class ConstantLoad implements RunMode {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantLoad.class);

    private static final String DEFAULT_LOAD_STRING = "50";
    private static final int DEFAULT_LOAD = Integer.parseInt(DEFAULT_LOAD_STRING);
    private AtomicBoolean loggedWarning = new AtomicBoolean(false);

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Collection<AsyncTestWorker> testWorkers = Collections.synchronizedSet(new HashSet<AsyncTestWorker>());
    private AsyncTestWorkerScheduler scheduler;
    private final List<RunMap> runMaps = new ArrayList<>();
    private int load = DEFAULT_LOAD;

    @ConfigArgSet(required = false, defaultValue = DEFAULT_LOAD_STRING, desc = "Set the load, in requests per second for the \"constantload\" runmode.")
    public void setLoad(String load) { this.load = Integer.parseInt(load); }

    @ConfigArgGet
    public int getLoad() { return this.load; }

    @Override
    public void runTests(Engine engine) throws Exception {
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


                    for (int i = 0; i < load; i++) {
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
