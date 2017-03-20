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
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.RunMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Worker for aggregating and publishing benchmarks.
 */
public class AsyncResultAggregator extends AsyncEngineWorker {
    private final Engine engine;
    private RunMode.RunContext context;

    /**
     * Constructor.
     * @param context list of test workers from this engine.
     */
    public AsyncResultAggregator(Engine engine, RunMode.RunContext context) {
        this.engine = engine;
        this.context = context;
    }

    /**
     * Method aggregating results.
     */
    private boolean aggregateResults() {
        Collection<RunMap> localRunMaps = context.getRunMaps();
        synchronized (localRunMaps) {
            for (RunMap localRunMap : localRunMaps) {
                Map<AbstractTest, Long> counts = engine.getPublishMode().aggregateAndReinitialize(localRunMap);

                Map<AbstractTest, AtomicLong> globalCounts = engine.getCounts();
                for (Map.Entry<AbstractTest, Long> entry : counts.entrySet()) {
                    globalCounts.get(entry.getKey()).addAndGet(entry.getValue());
                }
            }
        }
        return context.runIsFinished();
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
                engine.getPublishMode().publishIntermediateResults();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Engine.LOG.error("InterruptedException(s) should not reach this point", e);
        } catch (Throwable e) {
            Engine.LOG.error("Unexpected exception caught", e);
        } finally {
            // signal all publishers that they are stopped.
            // any local threads inside the publishers would have to be stopped
            stopPublishers();
        }
        aggregateResults();
    }

    private void stopPublishers() {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.finish();
        }
    }

}
