package com.adobe.qe.toughday.internal.core.engine.publishmodes;

import com.adobe.qe.toughday.api.core.Publisher;
import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.core.MetricResult;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;
import com.adobe.qe.toughday.internal.core.engine.PublishMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Description(desc = "Results are aggregated and published for the whole run.")
public class Simple extends PublishMode {

    @Override
    public void publishIntermediateResults(Map<String, List<MetricResult>> results) {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishAggregatedIntermediate(results);
        }
    }

    @Override
    public void publish(Collection<TestResult> testResults) {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishRaw(testResults);
        }
    }

    @Override
    public void publishFinalResults(Map<String, List<MetricResult>> results) {
        for (Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishAggregatedFinal(results);
        }
    }
}
