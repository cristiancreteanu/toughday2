package com.adobe.qe.toughday.publishers;

import com.adobe.qe.toughday.core.Publisher;
import com.adobe.qe.toughday.core.RunMap;
import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Publisher for writing at standard output.
 */
public class LiveChartPublisherPOC implements Publisher {
    private static final Logger LOG = LoggerFactory.getLogger(LiveChartPublisherPOC.class);
    private boolean finished = false;

    private String filePath = "results.csv";
    private static String HEADER = "Name, Duration / user, Runs, Fails, Min, Max, Median, Average, Real Throughput, Requests Throughput";
    HttpServer server;
    private Collection<? extends RunMap.TestStatistics> statistics;

    public Collection<? extends RunMap.TestStatistics> getStatistics() {
        return statistics;
    }

    public LiveChartPublisherPOC() throws IOException, InterruptedException {
        server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setServerInfo("LivePublish/1.1")
                .registerHandler("/liveresults", new JsonHandler(this))
                .registerHandler("/stats", new DashboardHandler())
                .create();
        Runnable serverRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                } catch (Exception e) {
                    LOG.error("Error starting publisher server", e);
                }

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        server.shutdown(5, TimeUnit.SECONDS);
                    }
                });
            }
        };
        Thread serverThread = new Thread(serverRunnable);
        serverThread.start();
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
        this.statistics = testStatistics;
    }
}

class JsonHandler implements HttpRequestHandler  {
    private final LiveChartPublisherPOC publisher;
    private final ObjectMapper mapper;

    public JsonHandler(LiveChartPublisherPOC publisher) {
        super();
        this.publisher = publisher;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        Collection<? extends RunMap.TestStatistics> statistics = publisher.getStatistics();
        Map<String, Object> testsMap = new HashMap<String, Object>();
        for (RunMap.TestStatistics stat : statistics) {
            Map<String, Double> testStat = new HashMap<String, Double>();

            testStat.put("duration", stat.getDurationPerUser());
            testStat.put("total_duration", stat.getTotalDuration());
            testStat.put("runs", (double) stat.getTotalRuns());
            testStat.put("failures", (double) stat.getFailRuns());
            testStat.put("avg", stat.getAverageDuration());
            testStat.put("median", (double) stat.getMedianDuration());
            testStat.put("min", stat.getMinDuration());
            testStat.put("max", stat.getMaxDuration());
            testStat.put("real_tp", stat.getRealThroughput());
            testStat.put("tp", stat.getExecutionThroughput());

            testsMap.put(stat.getTest().getFullName(), testStat);
        }

        // write json
        String out = mapper.writeValueAsString(testsMap);
        response.setEntity(new StringEntity(out));
    }
}

class DashboardHandler implements HttpRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardHandler.class);

    public DashboardHandler() {
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        InputStream html = Thread.currentThread().getContextClassLoader().getClass()
                .getResourceAsStream("/publishers/livechart/dashboard.html");
        response.setEntity(new InputStreamEntity(html));
        response.setStatusCode(HttpStatus.SC_OK);
    }
}
