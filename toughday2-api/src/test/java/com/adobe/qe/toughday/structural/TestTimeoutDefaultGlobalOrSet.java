package com.adobe.qe.toughday.structural;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTimeoutDefaultGlobalOrSet {
    private MockGlobalArgs globalArgs = new MockGlobalArgs();
    private MockTest test;

    @Before
    public void before() {
        test = new MockTest();
    }

    @Test
    public void testChooseGlobalTimeout() {
        Long testTimeout = test.getTimeout();
        Assert.assertEquals(testTimeout >= 0 ? testTimeout : globalArgs.getTimeout(), globalArgs.getTimeout());
    }

    @Test
    public void testChooseGlobalTimeoutAgain() {
        test.setTimeout("-5");
        Long testTimeout = test.getTimeout();
        Assert.assertEquals(testTimeout >= 0 ? testTimeout : globalArgs.getTimeout(), globalArgs.getTimeout());
    }

    @Test
    public void testChooseSetTimeout() {
        test.setTimeout("1");
        Long testTimeout = test.getTimeout();
        Assert.assertEquals(testTimeout >= 0 ? testTimeout : globalArgs.getTimeout(), 1000);
    }
}
