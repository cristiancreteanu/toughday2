package com.adobe.qe.toughday.core.engine.publishmodes;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.engine.Engine;

import java.util.Map;

public abstract class PublishMode {
    protected Engine engine;
    protected RunMap globalRunMap;

    public PublishMode(Engine engine) {
        this.globalRunMap = new RunMap();
        this.engine = engine;
    }

    public RunMap getGlobalRunMap() {
        return globalRunMap;
    }

    public Map<AbstractTest, Long> aggregateAndReinitialize(RunMap runMap) {
        return globalRunMap.aggregateAndReinitialize(runMap);
    }

    public abstract void publishIntermediateResults();

    public abstract void publishFinalResults();
}
