package com.adobe.qe.toughday.core.engine.publishmodes;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.benckmark.TestResult;
import com.adobe.qe.toughday.core.engine.PublishMode;
import com.adobe.qe.toughday.metrics.MetricResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Description(desc = "Results are aggregated and published for the whole run.")
public class Simple extends PublishMode {

    @Override
    public void publishIntermediateResults(Map<String, List<MetricResult>> results) {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishIntermediate(results);
        }
    }

    @Override
    public void publish(Collection<TestResult> testResults) {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publish(testResults);
        }
    }

    @Override
    public void publishFinalResults(Map<String, List<MetricResult>> results) {
        for (Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishFinal(results);
        }
    }
}
