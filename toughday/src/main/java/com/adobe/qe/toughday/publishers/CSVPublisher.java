package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.metrics.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Description(desc = "Publish statistics to a csv file")
public class CSVPublisher extends Publisher {
    public static final String DEFAULT_FILE_PATH = "results.csv";
    private static String header = "";
    private static String headerFormat = "";

    private static final Logger LOG = LoggerFactory.getLogger(CSVPublisher.class);

    private boolean finished = false;
    private boolean append = true;
    private boolean created = false;

    private PrintWriter printWriter;
    private BufferedWriter writer;
    private String filePath = DEFAULT_FILE_PATH;

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

    @Override
    public void publishIntermediate(Map<String, List<ResultInfo>> testsResults) {
        if (header.compareTo("") == 0) {
            createHeaderFormat(testsResults.values().iterator().next());
        }
        publish(testsResults);
    }

    @Override
    public void publishFinal(Map<String, List<ResultInfo>> testsResults) {
        publish(testsResults);
    }

    @Override
    public void finish() {
        this.finished = true;
    }

    private String createHeaderFormat(List<ResultInfo> resultInfoList) {

        for (ResultInfo resultInfo : resultInfoList) {
            header += resultInfo.getName() + ", ";
            headerFormat += resultInfo.getFormat() + ", ";
        }

        //remove last two characters
        header = header.substring(0, header.length() - 2);
        headerFormat = headerFormat.substring(0, headerFormat.length() - 2);

        return headerFormat;
    }

    public void publish(Map<String, List<ResultInfo>> testsResults) {
        try {
            if(!created || !append) {
                printWriter = new PrintWriter(filePath);
                created = true;
                writer = new BufferedWriter(printWriter);
                writer.write(header);
                writer.newLine();
                writer.flush();
            }
            for (String test : testsResults.keySet()) {
                List<Object> results = new ArrayList<>();
                List<ResultInfo> testResultInfos = testsResults.get(test);
                for (ResultInfo resultInfo : testResultInfos) {
                    results.add(resultInfo.getValue());
                }

                writer.write(String.format(headerFormat, results.toArray()));
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
