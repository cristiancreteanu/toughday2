package com.adobe.qe.toughday.core.engine.runmodes;

import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.engine.Engine;
import com.adobe.qe.toughday.core.engine.RunMode;

import java.util.concurrent.ExecutorService;

@Description(desc = "Prints the resulting configuration. Does not run any test.")
public class Dry implements RunMode {
    @Override
    public boolean isDryRun() {
        return true;
    }

    @Override
    public RunContext runTests(Engine engine) throws Exception {
        System.out.println("NOTE: This is just a dry run. No test is actually executed.");
        Engine.printConfiguration(engine.getConfiguration(), System.out);

        return null;
    }

    @Override
    public void finishExecution() {
        //Do nothing
    }

    @Override
    public ExecutorService getExecutorService() {
        return null;
    }
}
