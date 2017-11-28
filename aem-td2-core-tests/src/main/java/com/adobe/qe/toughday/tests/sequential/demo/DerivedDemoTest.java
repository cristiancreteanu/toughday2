package com.adobe.qe.toughday.tests.sequential.demo;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.annotations.*;
import com.adobe.qe.toughday.api.annotations.Internal;
import org.apache.sling.testing.clients.ClientException;

@Internal // <-- remove this to see the test in cli/help and to be able to run it
@Description(desc = "Demo derived description")
public class DerivedDemoTest extends DemoTest {

    public DerivedDemoTest() {
    }

    public DerivedDemoTest(String property) {
        super(property);
    }

    @Setup
    private void setupMethod() {
        logger().info(getFullName() + " Derived Setup");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    private void beforeMethod() {
        logger().info(getFullName() + " Derived Before");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    private void afterMethod() {
        logger().info(getFullName() + " Derived After");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void test() throws ClientException {
        logger().info(getFullName() + " Running derived test");
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
