package com.adobe.qe.toughday.api.core;

import com.adobe.qe.toughday.api.core.benchmark.TestResult;

import java.text.SimpleDateFormat;

public interface RunMap {
   public static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

   void record(TestResult testResult);

    /**
     * Get test statistics for a certain test.
     * @param test
     * @return
     */
    TestStatistics getRecord(AbstractTest test);

    /**
     * Test statistics
     */
     interface TestStatistics {

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

        /**
         * Get the lowest duration of test execution.
         * @return
         */
        long getMinDuration();

        /**
         * Get the highest duration of test execution.
         * @return
         */
        long getMaxDuration();

        /**
         * Get the average duration of all test executions.
         Formula: Sum (request time) / Runs
         * @return
         */
        double getAverageDuration();

        /**
         * Get the median duration of all test executions.
         * @return
         */
        long getMedianDuration();

        /**
         * Get total numbers of fail runs for this test.
         * @return
         */
        long getFailRuns();

        /**
         * Get total numbers of skipped runs for this test.
         * @return
         */
        long getSkippedRuns();


        long getValueAtPercentile(double percentile);

        /**
         * Get the standard deviation of the results of this test.
         * @return
         */
        double getStandardDeviation();
    }
}
