package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.CompositeTest;

public class CompositeHello extends CompositeTest {
    private HelloWorld test1;
    private HelloWorld test2;
    private HelloWorld test3;

    public CompositeHello() {
        this(true);
    }

    public CompositeHello(boolean createChildren) {
        if (createChildren) {
            this.test1 = new HelloWorld();
            this.test2 = new HelloWorld();
            this.test3 = new HelloWorld();

            test1.setName("Child1");
            test2.setName("Child2");
            test3.setName("Child3");

            this.addChild(test1);
            this.addChild(test2);
            this.addChild(test3);
        }
    }

    @Override
    public AbstractTest newInstance() {
        return new CompositeHello(false);
    }
}
