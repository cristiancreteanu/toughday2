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

import com.adobe.qe.toughday.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Async worker for running tests. There will be GlobalArgs.Concurrency test workers.
 */
class AsyncTestWorker extends AsyncEngineWorker {
    private static final Logger LOG = LogManager.getLogger(AsyncTestWorker.class);

    private final Engine engine;
    private RunMap localRunMap;
    private HashMap<UUID, AbstractTest> localTests;
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
    public AsyncTestWorker(Engine engine, TestSuite testSuite, RunMap localRunMap) {
        this.engine = engine;
        this.mutex = new ReentrantLock();
        this.testSuite = testSuite;
        localTests = new HashMap<>();
        for(AbstractTest test : testSuite.getTests()) {
            AbstractTest localTest = test.clone();
            localTests.put(localTest.getId(), localTest);
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
        Engine.LOG.debug("Thread running: " + workerThread);
        mutex.lock();
        try {
            while (!isFinished()) {
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
                    Engine.LOG.warn("Exceptions from tests should not reach this point", e);
                }
                mutex.lock();
                Thread.interrupted();
                Thread.sleep(engine.getGlobalArgs().getWaitTime());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Engine.LOG.error("InterruptedException(s) should not reach this point", e);
        } catch (Throwable e) {
            Engine.LOG.error("Unexpected exception caught", e);
        } finally {
            mutex.unlock();
        }
    }
}
