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
public class CSVPublisher extends Publisher {
    public static final String DEFAULT_FILE_PATH = "results.csv";
    private static final String INITIAL_FORMAT = "%s, %s, %d, %d, %d, %d, %d, %d, %f, %f, %d, %d, %d, %f";

    private static final Logger LOG = LoggerFactory.getLogger(CSVPublisher.class);


    private boolean finished = false;
    private boolean append = true;
    private boolean created = false;
    private int precision = 6;
    private String FORMAT = getFormat(precision);

    private PrintWriter printWriter;
    private BufferedWriter writer;

    private String filePath = DEFAULT_FILE_PATH;
    private static String HEADER = "Name, Timestamp, Passed, Failed, Skipped, Min, Max, Median, Average, StdDev, 90p, 99p, 99.9p, RealTP";

    @ConfigArgSet(required = false, desc = "The filename to write results to", defaultValue = DEFAULT_FILE_PATH)
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @ConfigArgGet
    public String getFilePath() {
        return this.filePath;
    }

    @ConfigArgSet(required = false, desc = "Append instead of rewrite", defaultValue = "true")
    public void setAppend(String value) {
        append = Boolean.valueOf(value);
    }

    @ConfigArgGet
    public boolean getAppend() {
        return append;
    }

    @ConfigArgSet(required = false, desc = "Precision for doubles and floats", defaultValue = "6")
    public void setPrecision(String precision) {
        this.precision = Integer.parseInt(precision);
        if (this.precision < 0 || this.precision > 12) {
            throw new IllegalArgumentException("Precision is not in range.");
        }
        FORMAT = getFormat(this.precision);
    }

    private static String getFormat(int precision) {
        return INITIAL_FORMAT.replace("f", "." + precision + "f");
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
                writer.write(String.format(FORMAT,
                        statistics.getTest().getFullName(),
                        statistics.getTimestamp(),
                        statistics.getTotalRuns(),
                        statistics.getFailRuns(),
                        statistics.getSkippedRuns(),
                        statistics.getMinDuration(),
                        statistics.getMaxDuration(),
                        statistics.getMedianDuration(),
                        statistics.getAverageDuration(),
                        statistics.getStandardDeviation(),
                        statistics.get90Percentile(),
                        statistics.get99Percentile(),
                        statistics.get999Percentile(),
                        statistics.getRealThroughput()));
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
