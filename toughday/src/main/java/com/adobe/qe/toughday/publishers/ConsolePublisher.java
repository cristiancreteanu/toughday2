package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.benckmark.TestResult;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.metrics.MetricResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Description(desc = "Publisher for writing at standard output.")
public class ConsolePublisher extends Publisher {
    private static final Logger LOG = LoggerFactory.getLogger(ConsolePublisher.class);
    private boolean begun = false;
    private boolean finished = false;
    private boolean clearScreen = true;
    private boolean batchLoggingCalled = false;
    private final CleanerThread cleaner;
    private Scanner sc;
    private AtomicInteger extraLines;


    @ConfigArgSet(required = false, defaultValue = "false", desc = "Enable the raw result publishing")
    public void setRawPublish(String rawPublish) {
        super.setRawPublish(rawPublish);
    }

    class CleanerThread extends Thread {

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(isr);
            try {
                while (!finished && !this.isInterrupted()) {
                        while (!in.ready()) {
                        if (finished) {
                            this.interrupt();
                            break;
                        }
                        this.sleep(200);
                    }

                    if (finished) {
                        this.interrupt();
                        break;
                    }

                    if (in.readLine() != null) {
                        extraLines.incrementAndGet();
                    }
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }

    public ConsolePublisher() {
        setRawPublish(Boolean.FALSE.toString()); //TODO remove this when call config arg set is merged
        sc = new Scanner(System.in);
        extraLines = new AtomicInteger(0);
        this.cleaner = new CleanerThread();
        this.cleaner.start();
    }

    @ConfigArgSet(required = false, defaultValue = "true", desc = "Clear the screen before printing each stat")
    public void setClear(String clearScreen) {
        this.clearScreen = Boolean.parseBoolean(clearScreen);
    }

    @ConfigArgGet
    public boolean getClear() {
        return this.clearScreen;
    }

    private void alignMetrics() {
        System.out.printf("\r\n");
        System.out.printf("%-35s", " ");
    }

    // publish results method, called periodically
    private void publish(Map<String, List<MetricResult>> results) {
        int nrStats = results.values().iterator().next().size();
        final int METRIC_LENGTH = 12;
        final int METRICS_PER_LINE_LIMIT = 3;
        final String FORMAT = "%-35s | ";
        // "clear" screen
        if (begun && clearScreen) {
            for (int i=0; i < (nrStats * 5) + 2 + extraLines.get(); i++ ) {
                System.out.print("\33[1A\33[2K");
            }
        }

        for (String testName : results.keySet()) {
            System.out.printf("%-35.35s", testName);
            List<MetricResult> metricResults = results.get(testName);
            metricResults.remove(0);
            int metricsPerLineCounter = 0;

            for (MetricResult resultInfo : metricResults) {
                String metricIdentifier = resultInfo.getName();
                String padding = StringUtils.repeat(' ', METRIC_LENGTH - metricIdentifier.length());
                String resultFormat = resultInfo.getFormat();
                String unitOfMeasure = resultInfo.getUnitOfMeasure();

                System.out.printf(FORMAT, metricIdentifier + ":" + padding +
                        String.format(resultFormat, resultInfo.getValue()) + " " + unitOfMeasure);
                metricsPerLineCounter++;

                if (metricsPerLineCounter == METRICS_PER_LINE_LIMIT) {
                    alignMetrics();
                    metricsPerLineCounter = 0;
                }
            }

            System.out.println();
            System.out.println();
        }

    }

    @Override
    protected void doPublishIntermediate(Map<String, List<MetricResult>> results) {
        publish(results);
    }

    @Override
    protected void doPublishFinal(Map<String, List<MetricResult>> results) {
        System.out.println("********************************************************************");
        System.out.println("                       FINAL RESULTS");
        System.out.println("********************************************************************");
        publish(results);
    }

    @Override
    protected void doPublish(Collection<TestResult> testResults) {

    }

    @Override
    public void finish() {
        this.finished = true;
        this.cleaner.interrupt();
    }

    private static String getFriendlyDuration(long millis) {
        if (millis < 0) {
            return "0 s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0)
            sb.append(days).append(" d ");
        if (hours > 0)
            sb.append(hours).append(" h ");
        if (minutes > 0)
            sb.append(minutes).append(" m ");
        sb.append(seconds);
        sb.append(" s");

        return(sb.toString());
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
