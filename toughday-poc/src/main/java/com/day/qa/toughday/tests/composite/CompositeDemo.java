package com.day.qa.toughday.tests.composite;

import com.day.qa.toughday.core.AbstractTest;
import com.day.qa.toughday.core.CompositeTest;
import com.day.qa.toughday.core.test_annotations.After;
import com.day.qa.toughday.core.test_annotations.Before;
import com.day.qa.toughday.core.test_annotations.Setup;
import com.day.qa.toughday.tests.sequential.*;

/**
 * Created by tuicu on 16/09/15.
 * Composite demo test.
 */
public class CompositeDemo extends CompositeTest {
    DemoTest test1;
    DemoTest test2;
    DemoTest test3;

    public CompositeDemo() {
        this(true);
    }

    @Override
    public AbstractTest newInstance() {
        return new CompositeDemo(false);
    }

    public CompositeDemo(boolean createChildren) {
        if(createChildren) {
            this.test1 = new DemoTest();
            this.test2 = new DemoTest();
            this.test3 = new DemoTest();

            test1.setName("Child1");
            test2.setName("Child2");
            test3.setName("Child3");

            this.addChild(test1);
            this.addChild(test2);
            this.addChild(test3);
        }
    }


    @Setup
    public void setupMethod() {
        System.out.println(getName() + " Setup");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Before
    public void beforeMethod() {
        System.out.println(getName() + " Before");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @After
    public void afterMethod() {
        System.out.println(getName() + " After");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
