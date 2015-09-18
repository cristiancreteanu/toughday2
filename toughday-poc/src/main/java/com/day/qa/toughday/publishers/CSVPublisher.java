package com.day.qa.toughday.publishers;

import com.day.qa.toughday.core.Publisher;
import com.day.qa.toughday.core.RunMap;
import com.day.qa.toughday.core.config.ConfigArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Created by tuicu on 01/09/15.
 */
public class CSVPublisher implements Publisher {
    private static final Logger logger = LoggerFactory.getLogger(CSVPublisher.class);
    private String filePath = "results.csv";
    private static String HEADER = "Name, Total Duration, Runs, Fails, Min, Max, Average, Median, Real Throughput, Reqs Throughput";

    @ConfigArg(required = false)
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
            for(RunMap.TestStatistics statistics : testStatistics) {
                writer.write(statistics.getTest().getName() + ", " +
                        statistics.getTotalDuration() + ", " +
                        statistics.getTotalRuns() + ", " +
                        statistics.getFailRuns() + ", " +
                        statistics.getMinDuration() + ", " +
                        statistics.getMaxDuration() + ", " +
                        statistics.getAverageDuration() + ", " +
                        statistics.getMedianDuration() + ", " +
                        statistics.getRealThroughput() + ", " +
                        statistics.getExecutionThroughput());
                writer.newLine();
            }
            writer.flush();
            writer.close();
            printWriter.close();
        } catch (IOException e) {
            logger.error("Could not publish results", e);
        }
    }
}
