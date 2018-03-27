package com.adobe.qe.toughday.tests;

import com.adobe.qe.toughday.api.annotations.Before;
import com.adobe.qe.toughday.api.annotations.After;
import com.adobe.qe.toughday.api.annotations.Setup;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;

import com.adobe.qe.toughday.api.core.AbstractTest;
import com.adobe.qe.toughday.api.core.FluentLogging;
import com.adobe.qe.toughday.api.core.SequentialTest;
import com.adobe.qe.toughday.api.core.benchmark.TestResult;
import org.apache.logging.log4j.Level;

class MyData {
    private String myproperty;

    public MyData(String myValue) {
        this.myproperty = myValue;
    }

    public String getMyProperty() {
        return myproperty;
    }
}

public class HelloWorld extends SequentialTest {
    private String myName;
    private Worker worker = new Worker();
    private EnhancedWorker superWorker = new EnhancedWorker();

    public HelloWorld() {
        //benchmark().registerClassProxy(Worker.class, WorkerProxy.class);
        //benchmark().registerClassProxyFactory(Worker.class, new WorkerProxyFactory());
        benchmark().registerHierarchyProxyFactory(Worker.class, new WorkerHierarchyProxyFactory());
    }

    public HelloWorld(String myName) {
        this.myName = myName;
    }

    @Setup
    public final void setup() {
        logger().info("Building a room...");
    }

    @Before
    public final void before() {
        logger().info("Entering the room...");
    }

    @Override
    public void test() throws Throwable {
        FluentLogging.create(logger())
                .before(Level.DEBUG, "Before the execution")
                .after(Level.DEBUG, "After the execution")
                .onSuccess(Level.INFO, "Execution finished with success")
                .onThrowable(Level.ERROR, "Error encountered during execution", true)
                .run(() -> {
                    benchmark().measure(this, "...", worker).doWork(37);
                });

        benchmark().measure(this, "Inhale", worker).doWork(37);
        benchmark().measure(this, "Pause", worker).doWork();
        benchmark().measure(this, "Super Work", superWorker).muchWork(100);

        benchmark().measure(this, "Speaking", (TestResult<Object> result)-> {
            result.withData(new MyData("myvalue"));
            logger().info("Hello World! I'm " + myName);
            Thread.sleep(20);
        });
        benchmark().measure(this, "Exhale", () -> {
            Thread.sleep(10);
        });
    }

    @After
    public final void after() {
        logger().info("Exiting the room...");
    }

    @Override
    public AbstractTest newInstance() {
        return new HelloWorld(myName);
    }

    @ConfigArgSet(required = false, defaultValue = "Bob", desc = "What's your name?")
    public void setMyName(String myName) {
        this.myName = myName;
    }

    @ConfigArgGet
    public String getMyName() {
        return myName;
    }
}
