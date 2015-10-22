package com.adobe.qe.toughday.tests.sequential;

import com.adobe.qe.toughday.core.AbstractTest;

/**
 * Created by tuicu on 12/08/15.
 */
public class CreateUserTest extends SequentialTestBase {
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
