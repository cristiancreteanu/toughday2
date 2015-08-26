package com.day.qa.toughday;

import com.day.qa.toughday.tests.AbstractTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuicu on 19/08/15.
 */
public class RunMap {
    private HashMap<AbstractTest, TestEntry> runMap;

    public RunMap() {
        runMap = new HashMap<>();
    }

    private RunMap(Collection<AbstractTest> tests) {
        this();
        for (AbstractTest test : tests) {
            runMap.put(test, new TestEntry(test));
        }
    }

    public void addTest(AbstractTest test) {
        runMap.put(test, new TestEntry(test));
    }

    public void recordRun(AbstractTest test, double duration) {
        runMap.get(test).recordRun(duration);
    }

    public void recordFail(AbstractTest test, Exception e) {
        runMap.get(test).recordFail(e);
    }

    public void aggregateAndReinitialize(RunMap other) {
        for(Map.Entry<AbstractTest, TestEntry> entry : other.runMap.entrySet()) {
            this.runMap.get(entry.getKey()).aggregateAndReinitialize(entry.getValue());
        }
    }

    public RunMap newInstance() {
        return new RunMap(runMap.keySet());
    }

    public interface TestStatistics {
        AbstractTest getTest();
        double getTotalDuration();
        long getTotalRuns();
        double getRealThroughput();
        double getExecutionThroughput();
        double getMinDuration();
        double getMaxDuration();
        double getAverageDuration();
        int getMedianDuration();
        long getFailRuns();
    }

    public Collection<? extends TestStatistics> getTestStatistics() {
        return runMap.values();
    }
    /**
     * Created by tuicu on 19/08/15.
     */
    private static class TestEntry implements TestStatistics{
            private AbstractTest test;
            private double totalDuration;
            private long totalRuns;
            private double minDuration;
            private double maxDuration;
            private long failRuns;
            private HashMap<Integer, Long> durationDistribution;
            private HashMap<Class<? extends Exception>, Long> failsMap;
            private long startNanoTime;
            private long lastNanoTime;

            private void init() {
                totalDuration = 0;
                totalRuns = 0;
                failRuns = 0;
                durationDistribution = new HashMap<>();
                failsMap = new HashMap<>();
                minDuration = Double.MAX_VALUE;
                maxDuration = Double.MIN_VALUE;
            }

            public TestEntry(AbstractTest test) {
                this.startNanoTime = System.nanoTime();
                this.test = test;
                init();
            }

            public void recordFail(Exception e) {
                if(!failsMap.containsKey(e.getClass())) {
                    failsMap.put(e.getClass(), 0L);
                }
                failsMap.put(e.getClass(), failsMap.get(e.getClass()) + 1);

                failRuns++;
            }

            public void recordRun(double duration) {
                lastNanoTime = System.nanoTime();
                totalRuns++;
                totalDuration += duration;
                minDuration = Math.min(minDuration, duration);
                maxDuration = Math.max(maxDuration, duration);
                Integer intDuration = ((Double)duration).intValue();
                if(!durationDistribution.containsKey(intDuration))
                    durationDistribution.put(intDuration, 0L);
                durationDistribution.put(intDuration, durationDistribution.get(intDuration) + 1);
            }

            public AbstractTest getTest() {
                return test;
            }

            public double getTotalDuration() {
                return totalDuration;
            }

            public long getTotalRuns() {
                return totalRuns;
            }

        @Override
        public double getRealThroughput() {
            return totalRuns * 1000000000l / (lastNanoTime - startNanoTime) ;
        }

        @Override
        public double getExecutionThroughput() {
            return getAverageDuration() * 1000;
        }

            public double getMinDuration() {
                return minDuration;
            }

            public double getMaxDuration() {
                return maxDuration;
            }

            public double getAverageDuration() {
                return totalDuration / totalRuns;
            }

            public long getFailRuns() {
                return failRuns;
            }

            public int getMedianDuration() {
                Integer[] keys = durationDistribution.keySet().toArray(new Integer[0]);
                Arrays.sort(keys);
                long pos = totalRuns / 2;
                for(Integer key : keys) {
                    if(pos < durationDistribution.get(key)) {
                        return key;
                    }
                    pos -= durationDistribution.get(key);
                }
                return -1;
            }

            public void aggregateAndReinitialize(TestEntry other) {
                this.lastNanoTime = Math.max(this.lastNanoTime, other.lastNanoTime);
                this.totalRuns += other.totalRuns;
                this.totalDuration += other.totalDuration;
                this.failRuns += other.failRuns;
                this.minDuration = Math.min(this.minDuration, other.minDuration);
                this.maxDuration = Math.max(this.maxDuration, other.maxDuration);
                for(Map.Entry<Integer, Long> entry : other.durationDistribution.entrySet()) {
                    if(!this.durationDistribution.containsKey(entry.getKey())) {
                        this.durationDistribution.put(entry.getKey(), 0L);
                    }
                    this.durationDistribution.put(entry.getKey(), this.durationDistribution.get(entry.getKey()) + entry.getValue());
                }
                for(Map.Entry<Class<? extends Exception>, Long> entry : other.failsMap.entrySet()) {
                    if(!this.failsMap.containsKey(entry.getKey())) {
                        this.failsMap.put(entry.getKey(), 0L);
                    }
                    this.failsMap.put(entry.getKey(), this.failsMap.get(entry.getKey()) + entry.getValue());
                }
                other.init();
            }
    }
}
