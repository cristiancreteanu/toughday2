package com.adobe.qe.toughday.core.engine;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by tuicu on 15/03/17.
 */
public interface RunMode {
    boolean isDryRun();
    List<AsyncTestWorker> runTests(Engine engine) throws Exception;
    ExecutorService getExecutorService();
}
