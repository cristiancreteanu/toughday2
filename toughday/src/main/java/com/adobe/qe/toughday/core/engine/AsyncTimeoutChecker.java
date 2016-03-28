/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.TestSuite;

import java.util.List;

/**
 * Worker for checking timeout and interrupting test worker threads when timeout is exceeded.
 * It uses Thread.interrupt for letting workers know the
 */
class AsyncTimeoutChecker extends AsyncEngineWorker {
    private Engine engine;
    private final Thread mainThread;
    private List<AsyncTestWorker> testWorkers;
    private long minTimeout;
    private TestSuite testSuite;

    /**
     * Constructor.
     * @param testSuite
     * @param testWorkers list of test workers from this engine.
     */
    public AsyncTimeoutChecker(Engine engine, TestSuite testSuite, List<AsyncTestWorker> testWorkers, Thread mainThread) {
        this.engine = engine;
        this.mainThread = mainThread;
        this.testWorkers = testWorkers;
        this.testSuite = testSuite;
        minTimeout = engine.getGlobalArgs().getTimeout();
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
        long timeout = testTimeout != null ? testTimeout : engine.getGlobalArgs().getTimeout();

        if (!worker.getMutex().tryLock()) {
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
                Thread.sleep(Math.round(Math.ceil(minTimeout * Engine.TIMEOUT_CHECK_FACTOR)));
                boolean testsFinished = true;
                for (AsyncTestWorker worker : testWorkers) {
                    interruptWorkerIfTimeout(worker);
                    if (!worker.isFinished()) {
                        testsFinished = false;
                    }
                }
                if (testsFinished) {
                    this.finishExecution();
                    mainThread.interrupt();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Engine.LOG.info ("Timeout thread interrupted");
        }
    }
}
