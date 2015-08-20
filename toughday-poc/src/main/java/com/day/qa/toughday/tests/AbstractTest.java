package com.day.qa.toughday.tests;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    public String getName() {
        return getClass().getSimpleName();
    }
    public long runTest() {
        Long start = System.nanoTime();
        test();
        return (System.nanoTime() - start) / 1000000l;
    }

    protected abstract void test();
}
