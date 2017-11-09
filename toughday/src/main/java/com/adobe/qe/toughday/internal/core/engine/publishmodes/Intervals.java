package com.adobe.qe.toughday.internal.core.engine.publishmodes;

import com.adobe.qe.toughday.api.annotations.Description;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.MetricResult;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.engine.Engine;

import java.util.List;
import java.util.Map;

@Description(desc = "Results are aggregated and published on intervals, rather than the whole execution. (Use --interval to specify the length of the aggregation interval).")
public class Intervals extends Simple {
    private static final String DEFAULT_INTERVAL = "5s";

    private String interval = "5s";
    private long delta;
    private long currentDelta = 0;

    public Intervals() {
        delta = computeDelta(Configuration.GlobalArgs.parseDurationToSeconds(interval));
    }

    private final long computeDelta(long interval) {
        return interval * 1000 / Engine.RESULT_AGGREATION_DELAY - 1;
    }


    @ConfigArgSet(required = false, defaultValue = DEFAULT_INTERVAL, desc = "Set the publishing interval. Can be expressed in s(econds), m(inutes), h(ours). Example: 1m30s.")
    public void setInterval(String interval) {
        this.interval = interval;
        this.delta = computeDelta(Configuration.GlobalArgs.parseDurationToSeconds(interval));
    }


    @ConfigArgGet
    public String getInterval() { return this.interval; }

    @Override
    public void publishIntermediateResults(Map<String, List<MetricResult>> results) {
        if (currentDelta < delta) {
            currentDelta++;
            return;
        }
        super.publishIntermediateResults(results);

        this.globalRunMap.reinitialize();
        this.currentDelta = 0;
    }
}
