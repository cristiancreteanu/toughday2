package com.adobe.qe.toughday.core;

import org.HdrHistogram.SynchronizedHistogram;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Map for storing benchmarks. Thread safe for benchmarking operations. Not thread safe for  adding and removing tests.
 */
public class RunMap {
    private static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /*
        The map should remain unordered (hash map) for faster access, that is why we are using a second
        data structure - list - to keep the order of the tests in output, just for the global run map.
        For now this is an internal implementation detail, we don't need to expose this unless
        a future run mode would require it.
     */
    private Map<AbstractTest, TestEntry> runMap;
    private List<AbstractTest> orderedTests;
    private boolean keepTestsOrdered = false;

    public RunMap () {
        runMap = new HashMap<>();
        orderedTests = new ArrayList<>();
        this.keepTestsOrdered = true;
    }

    private RunMap (Collection<AbstractTest> tests) {
        this();
        this.keepTestsOrdered = false;
        for (AbstractTest test : tests) {
            runMap.put(test, new TestEntry(test));
        }
    }

    public void addTest (AbstractTest test) {
        TestEntry entry = new TestEntry(test);
        runMap.put(test, entry);
        if(keepTestsOrdered) {
            orderedTests.add(test);
        }
    }

    public TestEntry getRecord(AbstractTest test) {
        return runMap.get(test);
    }
    
    /**
     * Returns a list that contains all tests(including the child tests of a composite test) in the exact order in which they were
     * added to the suite.
     * @return
     */
    public Collection<AbstractTest> getTests() {
        return orderedTests;
    }

    public void recordRun (AbstractTest test, double duration) {
        TestEntry entry = runMap.get(test);
        if(entry != null) {
            runMap.get(test).recordRun(duration);
        }
    }

    public void recordFail (AbstractTest test, Throwable e) {
        TestEntry entry = runMap.get(test);
        if(entry != null) {
            runMap.get(test).recordFail(e);
        }
    }

    public void recordSkipped(AbstractTest test, SkippedTestException e) {
        TestEntry entry = runMap.get(test);
        if (entry != null) {
            runMap.get(test).recordSkipped(e);
        }
    }

    public Map<AbstractTest, Long> aggregateAndReinitialize (RunMap other) {
        Map<AbstractTest, Long> counts = new HashMap<>();
        for (Map.Entry<AbstractTest, TestEntry> entry : other.runMap.entrySet()) {
            long count = this.runMap.get(entry.getKey()).aggregateAndReinitialize(entry.getValue());
            counts.put(entry.getKey(), count);
        }
        return counts;
    }

    public synchronized void reinitialize() {
        for (TestEntry testEntry : runMap.values()) {
            testEntry.init();
            testEntry.reinitStartTime();
        }
    }

    //TODO refactor this
    public void reinitStartTimes() {
        for (TestEntry entry : runMap.values()) {
            entry.reinitStartTime();
        }
    }

    public RunMap newInstance() {
        return new RunMap(runMap.keySet());
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
         * Get the timestamp of the last execution of this test.
         */
        String getTimestamp();

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

        /* TODO figure out how to compute this when the number of threads is not fixed
         * Get the execution throughput.
         * Calculated as follows: <code>number of parallel users / average duration</code>
         * @return

        double getExecutionThroughput();*/

        long getMinDuration();

        long getMaxDuration();

        double getAverageDuration();

        long getMedianDuration();

        long getFailRuns();

        long getSkippedRuns();

        long getValueAtPercentile(double percentile);

        double getStandardDeviation();
    }


    /**
     * A test statistics entry
     */
    public class TestEntry implements TestStatistics {
        public static final double ONE_BILLION_D = 1000 * 1000 * 1000.0d;
        private static final long ONE_MILION = 1000000;
        private AbstractTest test;
        private double totalDuration;
        private long failRuns;
        private long skippedRuns;
        private Map<Class<? extends Throwable>, Long> failsMap;
        private long startNanoTime;
        private long lastNanoTime;
        private long startMillisTime;
        private SynchronizedHistogram histogram;

        private void init() {
            totalDuration = 0;
            failRuns = 0;
            skippedRuns = 0;
            histogram.reset();
            failsMap = new HashMap<>();
        }

        /**
         *
         * @param test
         */
        public TestEntry(AbstractTest test) {
            this.test = test;
            histogram = new SynchronizedHistogram(3600000L /* 1h */, 3);
            reinitStartTime();
            init();
        }

        /**
         * Mark a skipped run
         * @param e
         */
        public synchronized void recordSkipped(SkippedTestException e) {
            lastNanoTime = System.nanoTime();
            skippedRuns++;
        }

        /**
         * Mark a failed run
         * @param e
         */
        public synchronized void recordFail(Throwable e) {
            lastNanoTime = System.nanoTime();
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
            histogram.recordValue((long) duration);
            lastNanoTime = System.nanoTime();
            totalDuration += duration;
        }

        //TODO refactor this?
        public synchronized void reinitStartTime() {
            this.startNanoTime = System.nanoTime();
            this.lastNanoTime = System.nanoTime();
            this.startMillisTime = System.currentTimeMillis();
        }

        @Override
        public AbstractTest getTest() {
            return test;
        }

        @Override
        public String getTimestamp() {
            return TIME_STAMP_FORMAT.format(new Date(startMillisTime + ((lastNanoTime - startNanoTime) / ONE_MILION)));
        }

        @Override
        public double getTotalDuration() {
            return totalDuration;
        }

        @Override
        public long getTotalRuns() {
            return histogram.getTotalCount();
        }

        @Override
        public double getRealThroughput() {
            return ((double) histogram.getTotalCount() * ONE_BILLION_D) / (lastNanoTime - startNanoTime);
        }

        /*
        @Override
        public double getExecutionThroughput() {
            return 1000 * threads / (getAverageDuration() + test.getGlobalArgs().getWaitTime());
        }*/

        @Override
        public long getMinDuration() {
            return histogram.getMinValue();
        }

        @Override
        public long getMaxDuration() {
            return histogram.getMaxValue();
        }

        @Override
        public double getAverageDuration() {
            return histogram.getMean();
        }

        @Override
        public long getFailRuns() {
            return failRuns;
        }

        @Override
        public long getSkippedRuns() {
            return skippedRuns;
        }

        public long getValueAtPercentile(double percentile) {
            return histogram.getValueAtPercentile(percentile);
        }

        @Override
        public double getStandardDeviation() {
            return histogram.getStdDeviation();
        }

        /* TODO delete if we don't find a use case, or figure out how to compute it when the number of threads is not fixed
        public double getDurationPerUser() {
            return totalDuration / threads;
        }*/

        @Override
        public long getMedianDuration() {
            return histogram.getValueAtPercentile(50);
        }

        public synchronized long aggregateAndReinitialize(TestEntry other) {
            long totalRuns = 0;
            synchronized (other) {
                totalRuns = other.histogram.getTotalCount();
                this.histogram.add(other.histogram);
                this.lastNanoTime = Math.max(this.lastNanoTime, other.lastNanoTime);
                this.totalDuration += other.totalDuration;
                this.failRuns += other.failRuns;
                this.skippedRuns += other.skippedRuns;
                other.init();
            }
            return totalRuns;
        }
    }
}
