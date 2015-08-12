package com.day.qa.toughday;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreatePageTest extends AbstractTest {
    public CreatePageTest() {
    }

    @Override
    public void test() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Creating page...");
    }
}
