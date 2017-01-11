package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@Description(desc = "Publish statistics to a csv file")
public class CSVPublisher implements Publisher {
    public static final String DEFAULT_FILE_PATH = "results.csv";

    private static final Logger LOG = LoggerFactory.getLogger(CSVPublisher.class);
    private boolean finished = false;
    private boolean append = false;
    private boolean created = false;
    private PrintWriter printWriter;
    private BufferedWriter writer;

    private String filePath = DEFAULT_FILE_PATH;
    private static String HEADER = "Name, Duration / user, Runs, Fails, Min, Max, Median, Average, Real Throughput, Requests Throughput";

    @ConfigArgSet(required = false, desc = "The filename to write results to", defaultValue = DEFAULT_FILE_PATH)
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @ConfigArgGet
    public String getFilePath() {
        return this.filePath;
    }

    @ConfigArgSet(required = false, desc = "Append instead of rewrite", defaultValue = "false")
    public void setAppend(String value) {
        append = Boolean.valueOf(value);
    }

    @ConfigArgGet
    public boolean getAppend() {
        return append;
    }

    @Override
    public void publishIntermediate(Collection<? extends RunMap.TestStatistics> testStatistics) {
        publish(testStatistics);
    }

    @Override
    public void publishFinal(Collection<? extends RunMap.TestStatistics> testStatistics) {
        publish(testStatistics);
    }

    @Override
    public void finish() {
        this.finished = true;
    }

    public void publish(Collection<? extends RunMap.TestStatistics> testStatistics) {
        try {
            if(!created || !append) {
                printWriter = new PrintWriter(filePath);
                created = true;
                writer = new BufferedWriter(printWriter);
                writer.write(HEADER);
                writer.newLine();
                writer.flush();
            }

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
            printWriter.flush();

            if(!append) {
                writer.close();
                printWriter.close();
            }
        } catch (IOException e) {
            LOG.error("Could not publish results", e);
        }
    }
}
