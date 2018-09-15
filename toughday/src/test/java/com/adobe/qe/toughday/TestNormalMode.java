package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.TestSuite;
import com.adobe.qe.toughday.internal.core.Timestamp;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.engine.AsyncTestWorker;
import com.adobe.qe.toughday.internal.core.engine.Engine;
import com.adobe.qe.toughday.internal.core.engine.runmodes.Normal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestNormalMode { // de extins cu teste pe threads
    private ArrayList<String> cmdLineArgs;
    @BeforeClass
    public static void onlyOnce() throws NoSuchMethodException {
        System.setProperty("logFileName", ".");
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @Before
    public void before() {
        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
    }

    @Test
    public void testDefault() throws Exception {
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getConcurrency(), 200);
    }

    @Test
    public void testNormalSimplePass() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "concurrency=100"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getConcurrency(), 100);
        Assert.assertEquals(configuration.getGlobalArgs().getDuration(), 20);
    }

    @Test
    public void testNormalSimpleFail() {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "load=10"));
        try {
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Load should not be configurable for Normal.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNormalStartEnd() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "start=10", "end=50"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getStart(), 10);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getEnd(), 50);
        Assert.assertEquals(configuration.getGlobalArgs().getDuration(), 20);
    }

    @Test
    public void testNormalStartConcurrency() {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "start=10", "concurrency=100"));
        try{
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Should not be able to have both start/end and concurrency.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNormalEndConcurrency() {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "end=50", "concurrency=100"));
        try{
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Should not be able to have both start/end and concurrency.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNormalStartEndConcurrency() {
        cmdLineArgs.addAll(Arrays.asList("--duration=20s", "--runmode", "type=normal", "start=10", "end=50", "concurrency=100"));
        try{
            Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Should not be able to have both start/end and concurrency.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNormalStartEndRate() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--runmode", "type=normal", "start=10", "end=100", "rate=5"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getStart(), 10);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getEnd(), 100);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getRate(), 5);
    }

    @Test
    public void testNormalStartEndRateInterval() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--runmode", "type=normal", "start=10", "end=100", "rate=5", "interval=1m"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);

        Normal runMode = (Normal) configuration.getRunMode();

        Assert.assertEquals(runMode.getStart(), 10);
        Assert.assertEquals(runMode.getEnd(), 100);
        Assert.assertEquals(runMode.getRate(), 5);
        Assert.assertEquals(runMode.getInterval(), 60000);
    }

    @Test
    public void runConcurrency() throws Exception {
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
        Engine engine = new Engine(configuration);

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);

        Normal runMode = (Normal) configuration.getRunMode();
        runMode.runTests(engine);

        Assert.assertFalse(runMode.getRunContext().isRunFinished());
        Assert.assertEquals(runMode.getRunContext().getRunMaps().size(), 200);
        Assert.assertEquals(runMode.getRunContext().getTestWorkers().size(), 200);
        Assert.assertEquals(runMode.getExecutorService().getClass(), ThreadPoolExecutor.class);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) runMode.getExecutorService();
        Assert.assertEquals(runMode.getActiveThreads(), 200);

        runMode.finishExecutionAndAwait();

        Assert.assertTrue(runMode.getRunContext().isRunFinished());

        threadPoolExecutor.shutdownNow();
        threadPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
    }

    @Test
    public void runStartEndDuration() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--runmode", "type=normal", "start=0", "end=30", "--duration=3s"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
        Engine engine = new Engine(configuration);

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);

        Normal runMode = (Normal) configuration.getRunMode();
        runMode.runTests(engine);

        runMode.runTests(engine);
        Thread.sleep(4000);

        Assert.assertEquals(runMode.getExecutorService().getClass(), ThreadPoolExecutor.class);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) runMode.getExecutorService();

        Assert.assertEquals(runMode.getActiveThreads(), 30);

        Assert.assertTrue(runMode.getRunContext().isRunFinished());

        threadPoolExecutor.shutdownNow();
        threadPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
    }

    @Test
    public void runStartEndRate() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--runmode", "type=normal", "start=0", "end=50", "rate=25"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
        Engine engine = new Engine(configuration);

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);

        Normal runMode = (Normal) configuration.getRunMode();
        runMode.runTests(engine);

        Assert.assertEquals(runMode.getRate(), 25);
        Assert.assertEquals(runMode.getInterval(), 1000);

        runMode.runTests(engine);
        Thread.sleep(3000);

        Assert.assertEquals(runMode.getExecutorService().getClass(), ThreadPoolExecutor.class);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) runMode.getExecutorService();

        Assert.assertEquals(runMode.getActiveThreads(), 50);

        Assert.assertTrue(runMode.getRunContext().isRunFinished());

        threadPoolExecutor.shutdownNow();
        threadPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
    }

    @Test
    public void runStartEndRateInterval() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--runmode", "type=normal", "start=30", "end=10", "rate=10", "interval=1s"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
        Engine engine = new Engine(configuration);

        Assert.assertEquals(configuration.getRunMode().getClass(), Normal.class);

        Normal runMode = (Normal) configuration.getRunMode();
        runMode.runTests(engine);

        Assert.assertEquals(runMode.getRate(), 10);
        Assert.assertEquals(runMode.getInterval(), 1000);

        runMode.runTests(engine);
        Thread.sleep(3000);

        Assert.assertEquals(runMode.getExecutorService().getClass(), ThreadPoolExecutor.class);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) runMode.getExecutorService();

        Assert.assertEquals(runMode.getActiveThreads(), 10);

        Assert.assertTrue(runMode.getRunContext().isRunFinished());

        threadPoolExecutor.shutdownNow();
        threadPoolExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
    }

    @After
    public void after() {
        new File("toughday_" + Timestamp.START_TIME + ".yaml").delete();
    }

    @AfterClass
    public static void deleteLogs() {
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
        LogFileEraser.deteleFiles(((LoggerContext) LogManager.getContext(false)).getConfiguration());

    }
}
