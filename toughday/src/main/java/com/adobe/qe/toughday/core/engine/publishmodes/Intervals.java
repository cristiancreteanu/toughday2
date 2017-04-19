package com.adobe.qe.toughday.core.engine.publishmodes;

import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.engine.Engine;

@Description(desc = "Results are aggregated and published on intervals, rather than the whole execution. (Use --interval to specify the length of the aggregation interval).")
public class Intervals extends Simple {
    private long delta;
    private long currentDelta = 0;

    public Intervals(Engine engine) {
        super(engine);
        this.delta = engine.getGlobalArgs().getInterval() * 1000 / Engine.RESULT_AGGREATION_DELAY - 1;
    }

    @Override
    public void publishIntermediateResults() {
        if (currentDelta < delta) {
            currentDelta++;
            return;
        }
        super.publishIntermediateResults();

        this.globalRunMap.reinitialize();
        this.currentDelta = 0;
    }
}
