package com.day.qa.toughday.tests;

import com.day.qa.toughday.tests.annotations.After;
import com.day.qa.toughday.tests.annotations.Before;
import com.day.qa.toughday.tests.annotations.Setup;

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

    @Setup
    public void setup() {
        System.out.println("Setup");
    }

    @Before
    public void before() {
        System.out.println("Before");
    }

    @After
    public void after() {
        System.out.println("After");
    }
}
