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

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.RunMap;

import java.util.List;

/**
 * Worker for aggregating and publishing benchmarks.
 */
class AsyncResultAggregator extends AsyncEngineWorker {
    private final Engine engine;
    private List<AsyncTestWorker> testWorkers;

    /**
     * Constructor.
     * @param testWorkers list of test workers from this engine.
     */
    public AsyncResultAggregator(Engine engine, List<AsyncTestWorker> testWorkers) {
        this.engine = engine;
        this.testWorkers = testWorkers;
    }

    /**
     * Method aggregating results.
     */
    private boolean aggregateResults() {
        boolean finished = true;
        for(AsyncTestWorker worker : testWorkers) {
            if (!worker.isFinished()) {
                finished = false;
            }
            RunMap localRunMap = worker.getLocalRunMap();
            engine.getGlobalRunMap().aggregateAndReinitialize(localRunMap);
        }
        return finished;
    }

    /**
     * Implementation of the Runnable interface.
     */
    @Override
    public void run() {
        try {
            while (!isFinished()) {
                Thread.sleep(Engine.RESULT_AGGREATION_DELAY);
                boolean testsFinished = aggregateResults();
                if (testsFinished) {
                    this.finishExecution();
                }
                publishIntermediateResults();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Engine.LOG.error("InterruptedException(s) should not reach this point", e);
        } finally {
            // signal all publishers that they are stopped.
            // any local threads inside the publishers would have to be stopped
            stopPublishers();
        }
        aggregateResults();
    }

    /**
     * Method for publishing intermediate results.
     */
    public void publishIntermediateResults() {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishIntermediate(engine.getGlobalRunMap().getTestStatistics());
        }
    }

    private void stopPublishers() {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.finish();
        }
    }

}