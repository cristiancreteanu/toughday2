package com.adobe.qe.toughday.core.engine.runmodes;

import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.core.engine.AsyncTestWorker;
import com.adobe.qe.toughday.core.engine.Engine;
import com.adobe.qe.toughday.core.engine.RunMode;
import junit.framework.TestSuite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Description(desc = "Prints the resulting configuration. Does not run any test.")
public class Dry implements RunMode {
    @Override
    public boolean isDryRun() {
        return true;
    }

    @Override
    public List<AsyncTestWorker> runTests(Engine engine) throws Exception {
        System.out.println("NOTE: This is just a dry run. No test is actually executed.");
        Engine.printConfiguration(engine.getConfiguration(), System.out);

        return new ArrayList<>();
    }

    @Override
    public ExecutorService getExecutorService() {
        return null;
    }
}
