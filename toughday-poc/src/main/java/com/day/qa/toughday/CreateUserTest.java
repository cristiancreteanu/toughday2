package com.day.qa.toughday;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreateUserTest extends AbstractTest {
    public CreateUserTest() {
        super();
    }

    @Override
    public void test() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Creating user...");
    }
}
