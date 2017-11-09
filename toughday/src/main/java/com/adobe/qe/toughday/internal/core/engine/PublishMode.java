package com.adobe.qe.toughday.internal.core.engine;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.MetricResult;
import com.adobe.qe.toughday.api.core.RunMap;
import com.adobe.qe.toughday.internal.core.RunMapImpl;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class PublishMode {
    protected Engine engine;
    protected RunMapImpl globalRunMap;

    public PublishMode() {
        this.globalRunMap = new RunMapImpl();
    }

    public RunMapImpl getGlobalRunMap() {
        return globalRunMap;
    }

    public Map<AbstractTest, Long> aggregateAndReinitialize(RunMap runMap) {
        return globalRunMap.aggregateAndReinitialize(runMap);
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public abstract void publishIntermediateResults(Map<String, List<MetricResult>> results);

    public abstract void publishFinalResults(Map<String, List<MetricResult>> results);

    public abstract void publish(Collection<TestResult> testResults);
}
