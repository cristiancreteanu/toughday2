package com.adobe.qe.toughday;

import com.adobe.qe.toughday.internal.core.ReflectionsContainer;
import com.adobe.qe.toughday.internal.core.config.Configuration;
import org.junit.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class TestConfiguration {
    private ArrayList<String> cmdLineArgs;
    private static ReflectionsContainer reflections = ReflectionsContainer.getInstance();

    @BeforeClass
    public static void onlyOnce() {
        reflections.getTestClasses().put("MockTest", MockTest.class);
        reflections.getTestClasses().put("MockTestTwin", MockTestTwin.class);
    }

    @Before
    public void before() {
        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
    }

    @Test
    public void testSimple() throws Exception {
        new Configuration(cmdLineArgs.toArray(new String[0]));

    }

    @Test
    public void testAdd() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertTrue((new Configuration(cmdLineArgs.toArray(new String[0]))).getTestSuite().contains("MockTest"));
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
    public void testConfigAfterAddPass() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "count=5"));
        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "timeout=6"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getTestSuite().getTest("MockTest").getCount(), 5);
        Assert.assertEquals(configuration.getTestSuite().getTest("MockTest").getTimeout(), 6000);
        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getTestSuite().getTotalWeight(), 1);

        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "count=10"));
        configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getTestSuite().getTest("MockTest").getCount(), 10);
        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getTestSuite().getTotalWeight(), 1);

        cmdLineArgs.addAll(Arrays.asList("--config", "MockTest", "weight=20"));
        configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getTestSuite().getTest("MockTest").getWeight(), 20);
        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getTestSuite().getTotalWeight(), 20);
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
    public void testAddConfigExclude() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest", "weight=5"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin", "count=5"));
        cmdLineArgs.addAll(Arrays.asList("--config", "MockTestTwin", "timeout=10"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "MockTestTwin"));
        Configuration configuration = new Configuration(cmdLineArgs.toArray(new String[0]));

        Assert.assertEquals(configuration.getTestSuite().getTests().size(), 1);
        Assert.assertEquals(configuration.getTestSuite().getTest("MockTest").getWeight(), 5);
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
    public void testConfigItem() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--add", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--config", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "CSVPublisher"));
        cmdLineArgs.addAll(Arrays.asList("--config", "CSVPublisher", "name=pub"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--config", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Average"));
        cmdLineArgs.addAll(Arrays.asList("--config", "Average", "name=med"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);
    }

    @Test
    public void testExcludeItem() throws Exception {
        cmdLineArgs.addAll(Arrays.asList("--add", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "ConsolePublisher"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size();

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "CSVPublisher", "name=pub"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "pub"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "Median"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTest"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);

        cmdLineArgs = new ArrayList<>(Collections.singletonList("--host=localhost"));
        cmdLineArgs.addAll(Arrays.asList("--add", "Average", "name=med"));
        cmdLineArgs.addAll(Arrays.asList("--exclude", "med"));
        cmdLineArgs.addAll(Arrays.asList("--add", "MockTestTwin"));
        Assert.assertEquals(new Configuration(cmdLineArgs.toArray(new String[0])).getTestSuite().getTests().size(), 1);
    }

    @AfterClass
    public static void afterAll() {
        new File("toughday_" + new SimpleDateFormat("yyyy-MM-dd'T'HH-mm").format(new Date()) + ".yaml").delete();
    }
}
