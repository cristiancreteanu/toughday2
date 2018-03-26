package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.api.core.MetricResult;
import com.adobe.qe.toughday.api.core.Publisher;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MyPublisher extends Publisher {
    private static final String RAW_FORMAT = "";

    @Override
    protected void doPublishAggregatedIntermediate(Map<String, List<MetricResult>> results) {
        doPublishAggregated(results);
    }


    @Override
    protected void doPublishAggregatedFinal(Map<String, List<MetricResult>> results) {
        doPublishAggregated(results);
    }

    private void doPublishAggregated(Map<String, List<MetricResult>> results) {
        for (String testName : results.keySet()) {
            List<MetricResult> testResultInfos = results.get(testName);
            for (MetricResult resultInfo : testResultInfos) {
                /*myExportAggregated(resultInfo.getName(),
                        resultInfo.getValue(),
                        resultInfo.getFormat(),
                        resultInfo.getUnitOfMeasure());*/
            }
        }
    }

    @Override
    protected void doPublishRaw(Collection<TestResult> testResults) {
        for (TestResult testResult : testResults) {
            Object data = testResult.getData();
            /*myExportRaw(String.format(RAW_FORMAT,
                    testResult.getTestFullName(),
                    testResult.getStatus().toString(),
                    testResult.getThreadId(),
                    testResult.getFormattedStartTimestamp(),
                    testResult.getFormattedEndTimestamp(),
                    testResult.getDuration(),
                    testResult.getData()));*/
        }
    }


    @Override
    public void finish() {
        // Clean up
    }
}
