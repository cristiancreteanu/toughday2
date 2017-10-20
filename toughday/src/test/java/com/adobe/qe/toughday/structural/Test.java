package com.adobe.qe.toughday.structural;

import org.junit.Before;

/**
 * Created by tuicu on 20/10/17.
 */
public class Test {

    @Before
    public void before() {
        System.out.println("Before");
        //throw new IllegalStateException("Before");
    }

    @org.junit.Test
    public void test() {
        System.out.println("Test");
    }

    @Before
    public void after() {
        System.out.println("After");
        throw new IllegalStateException("After");
    }
}
