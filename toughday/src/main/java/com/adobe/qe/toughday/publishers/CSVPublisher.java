package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.config.ConfigArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Publish statistics to a csv file
 */
public class CSVPublisher implements Publisher {
    private static final Logger LOG = LoggerFactory.getLogger(CSVPublisher.class);
    private String filePath = "results.csv";
    private static String HEADER = "Name, Duration / user, Runs, Fails, Min, Max, Median, Average, Real Throughput, Requests Throughput";

    @ConfigArg(required = false, desc = "The filename to write results to")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics) {
        publish(testStatistics);
    }

    @Override
    public void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics) {
        publish(testStatistics);
    }

    public void publish(Collection<? extends RunMap.TestStatistics> testStatistics) {
        try {
            PrintWriter printWriter = new PrintWriter(filePath);
            BufferedWriter writer = new BufferedWriter(printWriter);
            writer.write(HEADER);
            writer.newLine();
            for (RunMap.TestStatistics statistics : testStatistics) {
                writer.write(statistics.getTest().getFullName() + ", " +
                        statistics.getDurationPerUser() + ", " +
                        statistics.getTotalRuns() + ", " +
                        statistics.getFailRuns() + ", " +
                        statistics.getMinDuration() + ", " +
                        statistics.getMaxDuration() + ", " +
                        statistics.getMedianDuration() + ", " +
                        statistics.getAverageDuration() + ", " +
                        statistics.getRealThroughput() + ", " +
                        statistics.getExecutionThroughput());
                writer.newLine();
            }
            writer.flush();
            writer.close();
            printWriter.close();
        } catch (IOException e) {
            LOG.error("Could not publish results", e);
        }
    }
}
