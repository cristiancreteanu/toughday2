package com.day.qa.toughday.tests.serial_tests;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.test_annotations.Before;

/**
 * Created by tuicu on 12/08/15.
 */
public class OtherTest extends TestBase {
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

    @Before
    public void before() {
        System.out.println("Before");
    }

    @Override
    public AbstractTest newInstance() {
        return new OtherTest();
    }
}
