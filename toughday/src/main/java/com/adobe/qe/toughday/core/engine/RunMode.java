package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.core.RunMap;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Created by tuicu on 15/03/17.
 */
public interface RunMode {
    void runTests(Engine engine) throws Exception;
    void finishExecution();
    ExecutorService getExecutorService();
    RunContext getRunContext();

    interface RunContext {
        Collection<AsyncTestWorker> getTestWorkers();
        Collection<RunMap> getRunMaps();
        boolean isRunFinished();
    }
}
