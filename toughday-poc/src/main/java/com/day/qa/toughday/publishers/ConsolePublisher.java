package com.day.qa.toughday.publishers;

import com.day.qa.toughday.RunMap;

import java.util.Collection;

/**
 * Created by tuicu on 21/08/15.
 */
public class ConsolePublisher implements Publisher {
    private void publish(Collection<? extends RunMap.TestStatistics> testStatistics) {
        for(RunMap.TestStatistics statistics : testStatistics) {
            System.out.println(
                    "Test " + statistics.getTest().getName()
                            + " Total Duration: " + statistics.getTotalDuration()
                            + " Runs: " + statistics.getTotalRuns()
                            + " Fails: " + statistics.getFailRuns()
                            + " Min: " + statistics.getMinDuration()
                            + " Max: " + statistics.getMaxDuration()
                            + " Average: " + statistics.getAverageDuration()
                            + " Throughput: " + statistics.getThroughput()
                            + " Median: " + statistics.getMedianDuration()
            );
        }
    }

    @Override
    public void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics) {
        publish(testStatistics);
    }

    @Override
    public void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics) {
        System.out.println("********************************************************************");
        System.out.println("                       FINAL RESULTS");
        System.out.println("********************************************************************");
        publish(testStatistics);
    }
}
