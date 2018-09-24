package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.ReflectionsContainer;
import com.adobe.qe.toughday.internal.core.Timestamp;
import com.adobe.qe.toughday.internal.core.config.ConfigParams;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import com.adobe.qe.toughday.internal.core.engine.Phase;
import com.adobe.qe.toughday.internal.core.engine.runmodes.ConstantLoad;
import com.adobe.qe.toughday.internal.core.engine.runmodes.Normal;
import com.sun.tools.internal.jxc.ap.Const;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestConfiguration {
    private ArrayList<String> cmdLineArgs;
    private static ReflectionsContainer reflections = ReflectionsContainer.getInstance();

    @BeforeClass
    public static void onlyOnce() {
        reflections.getTestClasses().put("MockTest", MockTest.class);
        reflections.getTestClasses().put("MockTestTwin", MockTestTwin.class);

        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @Before
    public void before() {
        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
    }

    @Test
    public void testSimple() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        new Configuration(cmdLineArgs.toArray(new String[0]));

    }

    @Test
    public void testAdd() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertTrue((new Configuration(cmdLineArgs.toArray(new String[0]))).getPhases().iterator().next().getTestSuite().contains("MockTest"));
    }

    @Test
    public void testConfig() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "weight=5"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Cannot configure a test that has not been added.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testExclude() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTest"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Cannot exclude a test that has not been added.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testConfigAfterAddPass() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "count=5"));
        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "timeout=6"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTest("MockTest").getCount(), 5);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTest("MockTest").getTimeout(), 6000);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTotalWeight(), 1);

        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "count=10"));
        configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTest("MockTest").getCount(), 10);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTotalWeight(), 1);

        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "weight=20"));
        configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTest("MockTest").getWeight(), 20);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTotalWeight(), 20);
    }

    @Test
    public void testConfigAfterAddFail() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "count=5"));
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTestTwin", "timeout=6"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Cannot configure a test that has not been added.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddAfterConfig() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "timeout=5"));
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "count=5"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("The configuration of a test should come after its addition.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddAfterExclude() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "weight=5"));
            cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTestTwin"));
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin", "count=5"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("The exclusion of a test should come after its addition.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddExcludeConfig() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "weight=5"));
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin", "count=5"));
            cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTestTwin"));
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTestTwin", "timeout=10"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Cannot configure a test after having been excluded.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testAddConfigExclude() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "weight=5"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin", "count=5"));
        cmdLineArgs.addAll(Arrays.asList("--config", "MockTestTwin", "timeout=10"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTestTwin"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getPhases().iterator().next().getTestSuite().getTest("MockTest").getWeight(), 5);
    }

    @Test
    public void testExcludeConfigAdd() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTest"));
            cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "timeout=10"));
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin", "weight=5"));
            cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "count=5"));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("The exclusion of a test should come after its addition.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testConfigItem() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        cmdLineArgs.addAll(Arrays.asList("--add", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--config", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "CSVPublisher"));
        cmdLineArgs.addAll(Arrays.asList("--config", "CSVPublisher", "name=pub"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--config", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Average"));
        cmdLineArgs.addAll(Arrays.asList("--config", "Average", "name=med"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);
    }

    @Test
    public void testExcludeItem() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        cmdLineArgs.addAll(Arrays.asList("--add", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size();

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "CSVPublisher", "name=pub"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "pub"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Average", "name=med"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "med"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().iterator().next().getTestSuite().getTests().size(), 1);
    }

    @Test
    public void testBasicMetricsPass() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--host=localhost --add BASICMetrics --exclude Failed".split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getGlobalArgs().getMetrics().size(), 4);
    }

    @Test
    public void testBasicMetricsFail() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--host=localhost --add BASICMetrics --exclude Median".split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("BASICMetrics does not contain Median.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDEFAULTMetrics() throws Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --add DEFAULTMetrics --exclude Median" +
                " --config Average name=avg").split(" ")));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    //after adding phases option

    @Test
    public void testPhaseProperties() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--host=localhost --phase name=phase measurable=false duration=1m".split(" ")));
        Phase phase = new Configuration(cmdLineArgs.toArray(new String[0])).getPhases().get(0);

        Assert.assertEquals(phase.getName(), "phase");
        Assert.assertEquals(phase.getMeasurable(), false);

        long duration = phase.getDuration();
        Assert.assertEquals(duration, 60);
    }

    @Test
    public void testPhaseRunmode() throws Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --phase --runmode type=normal concurrency=100 " +
                "--phase --runmode type=constantload load=20").split(" ")));
        List<Phase> phases = new Configuration(cmdLineArgs.toArray(new String[0])).getPhases();

        Assert.assertEquals(phases.get(0).getRunMode().getClass(), Normal.class);
        Assert.assertEquals(((Normal)phases.get(0).getRunMode()).getConcurrency(), 100);
        Assert.assertEquals(((Normal)phases.get(0).getRunMode()).getWaitTime(), 300);

        Assert.assertEquals(phases.get(1).getRunMode().getClass(), ConstantLoad.class);
        Assert.assertEquals(((ConstantLoad)phases.get(1).getRunMode()).getLoad(), 20);
    }

    @Test
    public void testPhasesDurationTooLong() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--host=localhost --duration=10s --phase duration=5s --phase duration=10s".split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("The sum of the durations of phases should not exceed the global one.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testPhaseWithoutDuration() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--host=localhost --duration=10s --phase duration=5s --phase".split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhasesWithoutDuration().size(), 1);

        long duration = configuration.getPhases().get(1).getDuration();
        Assert.assertEquals(duration, 5L);
    }

    @Test
    public void testTwoPhasesWithoutDuration() throws Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --duration=15s --phase duration=5s --phase --phase").split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhasesWithoutDuration().size(), 2);

        long duration = configuration.getPhases().get(1).getDuration();
        Assert.assertEquals(duration, 5L);

        duration = configuration.getPhases().get(2).getDuration();
        Assert.assertEquals(duration, 5L);
    }

    @Test
    public void testNoPhaseWithoutDuration() throws Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --duration=15s --phase duration=5s --phase duration=5s" +
                " --phase duration=5s").split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getPhasesWithoutDuration().size(), 0);
    }

    @Test
    public void testTwoPhasesWithTheSameName() {
        try {
            cmdLineArgs.addAll(Arrays.asList(("--host=localhost --phase name=first " +
                    "--phase name=second --phase name=first").split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Two phases with the same name should not exist.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testUseconfigLoopTwo() {
        try {
            cmdLineArgs.addAll(Arrays.asList("--host=localhost --phase name=first useconfig=second --phase name=second useconfig=first".split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Loops should be detected.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testUseconfigLoopThree() {
        try {
            cmdLineArgs.addAll(Arrays.asList(("--host=localhost --phase name=first useconfig=second " +
                    "--phase name=second useconfig=third --phase name=third useconfig=first").split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("Loops should be detected.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testUseconfigNoLoop() throws Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --phase useconfig=second " +
                "--phase name=second useconfig=third " +
                "--phase name=third useconfig=fourth " +
                "--phase name=fourth").split(" ")));
        new Configuration(cmdLineArgs.toArray(new String[0]));
    }

    @Test
    public void testUseconfigNameNotFound() {
        try {
            cmdLineArgs.addAll(Arrays.asList(("--host=localhost --duration=1d --phase duration=10s " +
                    "--phase name=second useconfig=first " +
                    "--phase name=third duration=20s " +
                    "--phase measurable=false").split(" ")));
            new Configuration(cmdLineArgs.toArray(new String[0]));
            Assert.fail("The value of useconfig should be a name that was provided for a phase.");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGlobalTestsWithPhases() throws  Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --add MockTest name=mock1 --phase --add MockTest name=mock2 " +
                "--phase --add MockTest name=mock3 --add MockTest name=mock4").split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getPhases().get(0).getTestSuite().getTests().size(), 2);
        Assert.assertEquals(configuration.getPhases().get(1).getTestSuite().getTests().size(), 3);
    }

    @Test
    public void testGlobalTestsWithUseconfig() throws  Exception {
        cmdLineArgs.addAll(Arrays.asList(("--host=localhost --add MockTest name=mock1 " +
                "--phase useconfig=last --add MockTest name=mock5 --phase --add MockTest name=mock2 " +
                "--phase name=last --add MockTest name=mock3 --add MockTest name=mock4").split(" ")));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));
        List<Phase> phases = configuration.getPhases();

        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(phases.get(0).getTestSuite().getTests().size(), 4);
        Assert.assertNotNull(phases.get(0).getTestSuite().getTest("mock5"));
        Assert.assertNotNull(phases.get(0).getTestSuite().getTest("mock4"));
        Assert.assertNotNull(phases.get(0).getTestSuite().getTest("mock3"));

        Assert.assertEquals(phases.get(1).getTestSuite().getTests().size(), 2);
        Assert.assertEquals(phases.get(2).getTestSuite().getTests().size(), 3);
    }

    @After
    public void after() {
        ConfigParams.PhaseParams.namedPhases.clear();
    }

    @AfterClass
    public static void afterAll() {
        new File("toughday_" + Timestamp.START_TIME + ".yaml").delete();
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
        LogFileEraser.deteleFiles(((LoggerContext) LogManager.getContext(false)).getConfiguration());
    }
}
