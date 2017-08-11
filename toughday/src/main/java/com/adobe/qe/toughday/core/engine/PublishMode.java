package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.metrics.ResultInfo;

import java.util.List;
import java.util.Map;

public abstract class PublishMode {
    protected Engine engine;
    protected RunMap globalRunMap;

    public PublishMode() {
        this.globalRunMap = new RunMap();
    }

    public RunMap getGlobalRunMap() {
        return globalRunMap;
    }

    public Map<AbstractTest, Long> aggregateAndReinitialize(RunMap runMap) {
        return globalRunMap.aggregateAndReinitialize(runMap);
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public abstract void publishIntermediateResults(Map<String, List<ResultInfo>> testsResults);

    public abstract void publishFinalResults(Map<String, List<ResultInfo>> testsResults);
}
