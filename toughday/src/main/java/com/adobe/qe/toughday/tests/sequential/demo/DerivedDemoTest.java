package com.adobe.qe.toughday.tests.sequential.demo;

import com.adobe.granite.testing.ClientException;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Setup;
import com.adobe.qe.toughday.core.config.ConfigArg;

/**
 * Created by tuicu on 07/06/16.
 */
public class DerivedDemoTest extends DemoTest {

    public DerivedDemoTest() {
    }

    public DerivedDemoTest(String property) {
        super(property);
    }

    @Setup
    private void setupMethod() {
        System.out.println(getFullName() + " Derived Setup");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    private void beforeMethod() {
        System.out.println(getFullName() + " Derived Before");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    private void afterMethod() {
        System.out.println(getFullName() + " Derived After");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void test() throws ClientException {
        System.out.println(getFullName() + " Running derived test");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public AbstractTest newInstance() {
        return new DerivedDemoTest(getProperty());
    }
}
