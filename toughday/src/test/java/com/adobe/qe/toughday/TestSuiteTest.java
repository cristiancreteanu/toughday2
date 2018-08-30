package com.adobe.qe.toughday;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.*;
import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.internal.core.TestSuite;

public class TestSuiteTest {
    private TestSuite suite;
    private int totalWeight;
    private long totalTimeout;
    private long totalCount;

    @BeforeClass
    public static void beforeAll() {
        System.setProperty("logFileName", ".");
    }

    @Before
    public void before() {
        suite = new TestSuite();
        totalCount = totalTimeout = totalWeight = 0;

        suite.add((new MockTest()).setWeight(Integer.toString(10)).setCount(Long.toString(5)).setTimeout(Long.toString(20)).setName("a"));
        suite.add((new MockTest()).setWeight(Integer.toString(5)).setCount(Long.toString(50)).setTimeout(Long.toString(80)).setName("b"));
        suite.add((new MockTest()).setWeight(Integer.toString(1)).setCount(Long.toString(10)).setTimeout(Long.toString(50)).setName("c"));
        suite.add((new MockTest()).setWeight(Integer.toString(15)).setCount(Long.toString(20)).setTimeout(Long.toString(30)).setName("d"));
        suite.add((new MockTest()).setWeight(Integer.toString(50)).setCount(Long.toString(20)).setTimeout(Long.toString(10)).setName("e"));

        totalWeight = 81;
        totalTimeout = 190 * 1000;
        totalCount = 105;
    }

    @Test
    public void testTotalWeight() {
        Assert.assertEquals(suite.getTotalWeight(), totalWeight);
    }

    @Test
    public void testCount() {
        long total = 0;
        for (AbstractTest abstractTest : suite.getTests()) {
            total += abstractTest.getCount();
        }

        Assert.assertEquals(totalCount, total);
    }
    @Test
    public void testTimeout() {
        long total = 0;
        for (AbstractTest abstractTest : suite.getTests()) {
            total += abstractTest.getTimeout();
        }

        Assert.assertEquals(totalTimeout, total);
    }

    @Test
    public void testRemove() {
        AbstractTest abstractTest = suite.getTest("e");
        Assert.assertTrue(suite.contains(abstractTest.getName()));
        suite.remove(abstractTest.getName());

        totalCount -= abstractTest.getCount();
        totalTimeout -= abstractTest.getTimeout();
        totalWeight -= abstractTest.getWeight();
        Assert.assertTrue(!suite.contains(abstractTest.getName()));

        testTotalWeight();
        testTimeout();
        testCount();
    }

    @Test
    public void testAdd() {
        AbstractTest abstractTest = (new MockTest()).setWeight(Integer.toString(30)).setCount(Long.toString(65)).setTimeout(Long.toString(35)).setName("f");
        Assert.assertTrue(!suite.contains(abstractTest.getName()));

        suite.add(abstractTest);

        totalCount += abstractTest.getCount();
        totalTimeout += abstractTest.getTimeout();
        totalWeight += abstractTest.getWeight();
        Assert.assertTrue(suite.contains(abstractTest.getName()));

        testTotalWeight();
        testTimeout();
        testCount();
    }

    @Test
    public void testAddAll() {
        TestSuite secondSuite = new TestSuite();
        secondSuite.add((new MockTest()).setWeight(Integer.toString(15)).setCount(Long.toString(50)).setTimeout(Long.toString(200)).setName("x"));
        secondSuite.add((new MockTest()).setWeight(Integer.toString(50)).setCount(Long.toString(30)).setTimeout(Long.toString(10)).setName("y"));
        secondSuite.add((new MockTest()).setWeight(Integer.toString(10)).setCount(Long.toString(20)).setTimeout(Long.toString(40)).setName("z"));
        secondSuite.add((new MockTest()).setWeight(Integer.toString(10)).setCount(Long.toString(25)).setTimeout(Long.toString(70)).setName("w"));
        secondSuite.add((new MockTest()).setWeight(Integer.toString(10)).setCount(Long.toString(25)).setTimeout(Long.toString(10)).setName("v"));

        suite.addAll(secondSuite);

        for (AbstractTest test : secondSuite.getTests()) {
            totalWeight += test.getWeight();
            totalCount += test.getCount();
            totalTimeout += test.getTimeout();
        }

        testTotalWeight();
        testTimeout();
        testCount();
    }


    @Test
    public void testReplaceWeight() {
        AbstractTest test = suite.getTest("a");

        totalWeight -= test.getWeight();

        suite.replaceWeight("a", 201);

        totalWeight += 201;

        testTotalWeight();
    }

    @Test
    public void testReplaceName() {
        AbstractTest test = suite.getTest("d");

        suite.replaceName(test, "h");

        Assert.assertTrue(suite.contains(test.getName()));
    }

    @After
    public void deleteLogs()  {
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
        LogFileEraser.deteleFiles(((LoggerContext) LogManager.getContext(false)).getConfiguration());
    }
}
