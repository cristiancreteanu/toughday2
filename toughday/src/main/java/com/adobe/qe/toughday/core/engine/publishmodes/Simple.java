package com.adobe.qe.toughday.core.engine.publishmodes;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.engine.PublishMode;
import com.adobe.qe.toughday.metrics.ResultInfo;

import java.util.List;
import java.util.Map;

@Description(desc = "Results are aggregated and published for the whole run.")
public class Simple extends PublishMode {

    @Override
    public void publishIntermediateResults(Map<String, List<ResultInfo>> testsResults) {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishIntermediate(testsResults);
        }
    }

    @Override
    public void publishFinalResults(Map<String, List<ResultInfo>> testsResults) {
        for (Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishFinal(testsResults);
        }
    }
}
