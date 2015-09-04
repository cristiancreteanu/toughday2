package com.day.qa.toughday.tests;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreateUserTest extends TestBase {
    public CreateUserTest() {
        super();
    }

    @Override
    public void test() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("Creating user...");
    }

    @Override
    public AbstractTest newInstance() {
        return new CreateUserTest();
    }
}
