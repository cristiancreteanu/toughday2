package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.annotations.Description;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Description(desc = "Publisher for writing at standard output.")
public class ConsolePublisher extends Publisher {
    private static final Logger LOG = LoggerFactory.getLogger(ConsolePublisher.class);
    private boolean begun = false;
    private boolean finished = false;
    private boolean clearScreen = true;
    private final CleanerThread cleaner;
    private Scanner sc;
    private AtomicInteger extraLines;


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
        sc = new Scanner(System.in);
        extraLines = new AtomicInteger(0);
        this.cleaner = new CleanerThread();
        this.cleaner.start();
    }

    @ConfigArgSet(required = false, defaultValue = "true",desc = "Clear the screen before printing each stat")
    public void setClear(String clearScreen) {
        this.clearScreen = Boolean.parseBoolean(clearScreen);
    }

    @ConfigArgGet
    public boolean getClear() {
        return this.clearScreen;
    }

    // publish results method, called periodically
    private void publish(Collection<? extends RunMap.TestStatistics> testStatistics) {
        int nrStats = testStatistics.size();

        // "clear" screen
        if (begun && clearScreen) {
            for (int i=0; i < (nrStats * 4) + 2 + extraLines.get(); i++ ) {
                System.out.print("\33[1A\33[2K");
            }
        }

        // print stats
        final String SPACE = " ";
        System.out.println();
        for (RunMap.TestStatistics statistics : testStatistics) {
            double minDuration = (statistics.getMinDuration()  == Double.MAX_VALUE) ? 0 : statistics.getMinDuration();
            System.out.printf("%-35.35s | %-28s | %-25s | %-25s |\r\n%35s | %-28s | %-25s | %-25s |\r\n%35s | %-28s | %-25s | %-25s |\r\n",
                    statistics.getTest().getFullName(),
                    "Timestamp: " + statistics.getTimestamp(),
                    "Runs:     " + String.format("%d", statistics.getTotalRuns()),
                    "Fails:    " + String.format("%d", statistics.getFailRuns()),
                    SPACE,
                    "Min:      " + String.format("%d", (long) minDuration) + " ms",
                    "Max:      " + String.format("%d", (long) statistics.getMaxDuration()) + " ms",
                    "Median:   " + String.format("%d", (long) statistics.getMedianDuration()) + " ms",
                    " ",
                    "Average:  " + String.format("%.1f", statistics.getAverageDuration()) + " ms",
                    "Real TP:  " + String.format("%.1f", statistics.getRealThroughput()) + " rps",
                    "Reqs TP:  " + String.format("%.1f", statistics.getExecutionThroughput()) + " rps"
            );
            System.out.println();
        }
        System.out.println();
        begun = true;


        extraLines.set(0);
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
