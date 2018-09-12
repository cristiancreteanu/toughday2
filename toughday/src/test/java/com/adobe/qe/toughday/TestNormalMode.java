package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.Timestamp;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.engine.runmodes.Normal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TestNormalMode {
    private ArrayList<String> cmdLineArgs;

    @BeforeClass
    public static void onlyOnce() {
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
        Assert.assertEquals(((Normal)configuration.getRunMode()).getStart(), 10);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getEnd(), 100);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getRate(), 5);
        Assert.assertEquals(((Normal)configuration.getRunMode()).getInterval(), 60);
    }

    @AfterClass
    public static void after() {
        new File("toughday_" + Timestamp.START_TIME + ".yaml").delete();
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
        LogFileEraser.deteleFiles(((LoggerContext) LogManager.getContext(false)).getConfiguration());
    }
}
