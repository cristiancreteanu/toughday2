package com.day.qa.toughday.tests.sequential;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.test_annotations.Before;

/**
 * Created by tuicu on 12/08/15.
 */
public class OtherTest extends SequentialTestBase {
    public OtherTest() {
        super();
    }

    @Override
    public void test() throws InterruptedException {
        Thread.sleep(30);
        System.out.println("Other test...");
    }

    @Before
    public void before() {
        //System.out.println("Before");
    }

    @Override
    public AbstractTest newInstance() {
        return new OtherTest();
    }
}
