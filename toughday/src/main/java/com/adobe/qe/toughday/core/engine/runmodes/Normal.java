package com.adobe.qe.toughday.core.engine.runmodes;

import com.adobe.qe.toughday.core.TestSuite;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.core.engine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Description(desc = "Runs tests normally.")
public class Normal implements RunMode {
    private ExecutorService testsExecutorService;

    @Override
    public boolean isDryRun() {
        return false;
    }

    @Override
    public List<AsyncTestWorker> runTests(Engine engine) throws Exception {
        Configuration configuration = engine.getConfiguration();
        TestSuite testSuite = configuration.getTestSuite();
        Configuration.GlobalArgs globalArgs = configuration.getGlobalArgs();
        testsExecutorService = Executors.newFixedThreadPool(globalArgs.getConcurrency());

        // Create the test worker threads
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for (int i = 0; i < configuration.getGlobalArgs().getConcurrency(); i++) {
            AsyncTestWorker runner = new AsyncTestWorker(engine, testSuite, engine.getGlobalRunMap().newInstance());
            testWorkers.add(runner);
            testsExecutorService.execute(runner);
        }
        return testWorkers;
    }

    @Override
    public ExecutorService getExecutorService() {
        return testsExecutorService;
    }
}
