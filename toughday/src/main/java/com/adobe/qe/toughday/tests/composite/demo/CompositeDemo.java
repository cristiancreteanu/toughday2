package com.adobe.qe.toughday.tests.composite.demo;

import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.CompositeTest;
import com.adobe.qe.toughday.core.annotations.*;
import com.adobe.qe.toughday.tests.sequential.demo.DemoTest;

@Internal // <-- remove this to see the test in cli/help and to be able to run it
@Description(desc = "Demo composite description")
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
    private void setupMethod() {
        System.out.println(getFullName() + " Setup");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Before
    private void beforeMethod() {
        System.out.println(getFullName() + " Before");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @After
    private void afterMethod() {
        System.out.println(getFullName() + " After");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
