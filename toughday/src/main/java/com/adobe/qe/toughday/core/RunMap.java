package com.adobe.qe.toughday.core;

import java.util.*;

/**
 * Map for storing benchmarks. Thread safe for benchmarking operations. Not thread safe for  adding and removing tests.
 */
public class RunMap {
    private Map<AbstractTest, TestEntry> runMap;
    private int threads;

    public RunMap (int threads) {
        this.threads = threads;
        runMap = new HashMap<>();
    }

    private RunMap (int threads, Collection<AbstractTest> tests) {
        this(threads);
        for (AbstractTest test : tests) {
            runMap.put(test, new TestEntry(test));
        }
    }

    public void addTest (AbstractTest test) {
        runMap.put(test, new TestEntry(test));
    }

    public TestEntry getRecord(AbstractTest test) {
        return runMap.get(test);
    }

    public void recordRun (AbstractTest test, double duration) {
        runMap.get(test).recordRun(duration);
    }

    public void recordFail (AbstractTest test, Exception e) {
        runMap.get(test).recordFail(e);
    }

    public void aggregateAndReinitialize (RunMap other) {
        for (Map.Entry<AbstractTest, TestEntry> entry : other.runMap.entrySet()) {
            this.runMap.get(entry.getKey()).aggregateAndReinitialize(entry.getValue());
        }
    }

    public RunMap newInstance() {
        return new RunMap(threads, runMap.keySet());
    }

    /**
     * Test statistics
     */
    public interface TestStatistics {

        /**
         * Get the test reference for this run
         * @return
         */
        AbstractTest getTest();

        /**
         * Get total number of seconds that all the threads have spent in the test
         * @return
         */
        double getTotalDuration();

        /**
         * Get total numbers of run for this test
         * @return
         */
        long getTotalRuns();

        /**
         * Get the real throughput
         * Calculated as follows: <code>number of total runs / total time duration, including setup and wait times </code>
         * @return
         */
        double getRealThroughput();

        /**
         * Get the execution throuhput.
         * Calculated as follows: <code>number of parallel users / average duration</code>
         * @return
         */
        double getExecutionThroughput();

        double getMinDuration();

        double getMaxDuration();

        double getAverageDuration();

        double getDurationPerUser();

        long getMedianDuration();

        long getFailRuns();
    }

    public Collection<? extends TestStatistics> getTestStatistics() {
        return runMap.values();
    }


    /**
     * A test statistics entry
     */
    public class TestEntry implements TestStatistics {
        private AbstractTest test;
        private double totalDuration;
        private long totalRuns;
        private double minDuration;
        private double maxDuration;
        private long failRuns;
        private Map<Long, Long> durationDistribution;
        private Map<Class<? extends Exception>, Long> failsMap;
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

        /**
         *
         * @param test
         */
        public TestEntry(AbstractTest test) {
            this.test = test;
            this.startNanoTime = System.nanoTime();
            init();
        }

        /**
         * Mark a failed run
         * @param e
         */
        public synchronized void recordFail(Exception e) {
            if (!failsMap.containsKey(e.getClass())) {
                failsMap.put(e.getClass(), 0L);
            }
            failsMap.put(e.getClass(), failsMap.get(e.getClass()) + 1);

            failRuns++;
        }

        /**
         * Record numbers for a successful run
         * @param duration
         */
        public synchronized void recordRun(double duration) {
            lastNanoTime = System.nanoTime();
            totalRuns++;
            totalDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
            Long intDuration = ((Double) duration).longValue();
            if (!durationDistribution.containsKey(intDuration)) {
                durationDistribution.put(intDuration, 0L);
            }
            durationDistribution.put(intDuration, durationDistribution.get(intDuration) + 1);
        }

        @Override
        public AbstractTest getTest() {
            return test;
        }

        @Override
        public double getTotalDuration() {
            return totalDuration;
        }

        @Override
        public long getTotalRuns() {
            return totalRuns;
        }

        @Override
        public double getRealThroughput() {
            return ((double) totalRuns * 1000000000.0d) / (lastNanoTime - startNanoTime);
        }

        @Override
        public double getExecutionThroughput() {
            return 1000 * threads / getAverageDuration();
        }

        @Override
        public double getMinDuration() {
            return minDuration;
        }

        @Override
        public double getMaxDuration() {
            return maxDuration;
        }

        @Override
        public double getAverageDuration() {
            return totalDuration / totalRuns;
        }

        @Override
        public long getFailRuns() {
            return failRuns;
        }

        @Override
        public double getDurationPerUser() {
            return totalDuration / threads;
        }

        @Override
        public long getMedianDuration() {
            Long[] keys = durationDistribution.keySet().toArray(new Long[0]);
            Arrays.sort(keys);
            long pos = totalRuns / 2;
            for (Long key : keys) {
                if (pos < durationDistribution.get(key)) {
                    return key;
                }
                pos -= durationDistribution.get(key);
            }
            return -1;
        }

        public synchronized void aggregateAndReinitialize(TestEntry other) {
            synchronized (other) {
                this.lastNanoTime = Math.max(this.lastNanoTime, other.lastNanoTime);
                this.totalRuns += other.totalRuns;
                this.totalDuration += other.totalDuration;
                this.failRuns += other.failRuns;
                this.minDuration = Math.min(this.minDuration, other.minDuration);
                this.maxDuration = Math.max(this.maxDuration, other.maxDuration);
                for (Map.Entry<Long, Long> entry : other.durationDistribution.entrySet()) {
                    if (!this.durationDistribution.containsKey(entry.getKey())) {
                        this.durationDistribution.put(entry.getKey(), 0L);
                    }
                    this.durationDistribution.put(entry.getKey(), this.durationDistribution.get(entry.getKey()) + entry.getValue());
                }
                for (Map.Entry<Class<? extends Exception>, Long> entry : other.failsMap.entrySet()) {
                    if (!this.failsMap.containsKey(entry.getKey())) {
                        this.failsMap.put(entry.getKey(), 0L);
                    }
                    this.failsMap.put(entry.getKey(), this.failsMap.get(entry.getKey()) + entry.getValue());
                }
                other.init();
            }
        }
    }
}
