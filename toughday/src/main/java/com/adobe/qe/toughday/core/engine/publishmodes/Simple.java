package com.adobe.qe.toughday.core.engine.publishmodes;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.engine.Engine;

@Description(desc = "Results are aggregated and published for the whole run.")
public class Simple extends PublishMode {
    public Simple(Engine engine) {
        super(engine);
    }

    @Override
    public void publishIntermediateResults() {
        for(Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishIntermediate(getGlobalRunMap().getTestStatistics());
        }
    }

    @Override
    public void publishFinalResults() {
        for (Publisher publisher : engine.getGlobalArgs().getPublishers()) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }
}
