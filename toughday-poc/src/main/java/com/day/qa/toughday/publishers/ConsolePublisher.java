package com.day.qa.toughday.publishers;

import com.day.qa.toughday.core.Publisher;
import com.day.qa.toughday.core.RunMap;

import java.util.Collection;

/**
 * Created by tuicu on 21/08/15.
 */
public class ConsolePublisher implements Publisher {
    private void publish(Collection<? extends RunMap.TestStatistics> testStatistics) {
        for(RunMap.TestStatistics statistics : testStatistics) {
            System.out.println(
                    "Test " + statistics.getTest().getName()
                            + " Total Duration: " + statistics.getTotalDuration()  + " ms"
                            + " Runs: " + statistics.getTotalRuns()
                            + " Fails: " + statistics.getFailRuns()
                            + " Min: " + String.format("%.1f", statistics.getMinDuration() ) + " ms"
                            + " Max: " + String.format("%.1f", statistics.getMaxDuration() )  + " ms"
                            + " Average: " + String.format("%.1f", statistics.getAverageDuration())  + " ms"
                            + " Median: " + statistics.getMedianDuration() + " ms"
                            + " Real Throughput: " + String.format("%.1f", statistics.getRealThroughput()) + " runs/s"
                            + " Reqs Throughput: " + String.format("%.1f", statistics.getExecutionThroughput()) + " runs/s"
            );
        }
        System.out.println();
        System.out.println();
        System.out.println();
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
