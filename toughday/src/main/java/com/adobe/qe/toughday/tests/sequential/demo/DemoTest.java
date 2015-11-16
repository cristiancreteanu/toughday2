package com.adobe.qe.toughday.tests.sequential.demo;

import com.adobe.granite.testing.ClientException;
import com.adobe.qe.toughday.core.AbstractTest;
import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.annotations.After;
import com.adobe.qe.toughday.core.annotations.Before;
import com.adobe.qe.toughday.core.annotations.Setup;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;

/**
 * A demo test class that explains how tests must be written. It extends the SequentialTestBase,
 * meaning will have the test method as defined there and it will be run by SequentialTestRunner.
 * Since it is also a subtype of AbstractTest, it will be automatically picked up by the core
 * and can be added to the suite from the command line with --add DemoTest Property=myValue
 */
public class DemoTest extends SequentialTestBase {
    private String property;

    /**
     *  Public constructor with no args.
     *  This will be called when the test is instantiated by the configuration parser.
     */
    public DemoTest() {
    }

    /**
     * Constructor with arguments to pass the internal field values when replicated
     * to be called from the newInstance method that all tests must implement.
     * You can write a constructor like this, or use the builder pattern in setters.
     * @param property
     */
    public DemoTest(String property) {
        this.property = property;
    }


    /**
     *  Setup method.
     *  This will run only once, before any instances of this test are ran.
     */
    @Setup
    public void setupMethod() {
        System.out.println(getName() + " Setup");
        try {
            /* Sleeps are not required, actually they are quite harmful, because they reduce the rate with which
            the tests and affect the throughput. Use them only if the tests simulates wait between requests, otherwise
            use the --WaitTime parameter to specify wait between test runs.
             */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Before method.
     *  This will run before every test run.
     */
    @Before
    public void beforeMethod() {
        System.out.println(getName() + " Before");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *  After method
     *  This will run after the test, even if the test fails
     */
    @After
    public void afterMethod() {
        System.out.println(getName() + " After");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Test method as defined by the SequentialTestBase. Different test bases
     *  can define test methods differently. The runner of the test needs to know
     *  how to properly run it. If none of the test bases are fitted for your particular
     *  test, you can choose to extend AbstractTest, but you will have to implement the runner
     *  for your new type of test as well.
     */
    @Override
    public void test() throws ClientException {
        System.out.println(getName() + " Running test with Property=" + property);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Command line argument property.
     * Annotating your setter with ConfigArg will expose it to the command line as a property for this test.
     * Properties annotated with ConfigArg are by default required, if the user doesn't give a value for it
     * Toughday won't run. You can set the property to optional if you have a default for it
     * @param property value as given by the user in the command line. Must be of type String
     * @return usually tests respect the builder pattern and return "this", but it's not a requirement
     */
    @ConfigArg(/*required = true*/)
    public DemoTest setProperty(String property) {
        this.property = property;
        return this;
    }

    /**
     * Method called by the core in order to replicate this test for all the threads. Should return a
     * new instance of this test with all the properties already set.
     * You don't have to worry about the ID and the Name, those are taken care of.
     * @return
     */
    @Override
    public AbstractTest newInstance() {
        //Call the constructor with arguments
        return new DemoTest(property);
        //or you can use the builder pattern
        //return new DemoTest().setProperty(property);
    }
}
