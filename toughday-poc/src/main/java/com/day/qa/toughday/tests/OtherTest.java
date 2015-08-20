package com.day.qa.toughday.tests;

/**
 * Created by tuicu on 12/08/15.
 */
public class OtherTest extends AbstractTest {
    public OtherTest() {
        super();
    }

    @Override
    public void test() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Other test...");
    }

}
