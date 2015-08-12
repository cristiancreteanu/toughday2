package com.day.qa.toughday;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    public long runTest() {
        Long start = System.nanoTime();
        test();
        return System.nanoTime() - start;
    }

    protected abstract void test();
}
