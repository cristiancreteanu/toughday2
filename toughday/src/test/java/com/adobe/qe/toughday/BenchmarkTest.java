package com.adobe.qe.toughday;


import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.internal.core.RunMapImpl;
import com.adobe.qe.toughday.internal.core.benckmark.BenchmarkImpl;
import com.adobe.qe.toughday.tests.sequential.demo.DemoTest;
import org.junit.Test;


/**
 * Main class. Creates a Configuration and an engine and runs the tests.
 */
public class BenchmarkTest {
    public static class MyClass {
        public void method(String s) {
        }

        public void method(String s, int i) {
        }

        public void method(String s, long x, int... args) {

        }

        public void method(int... args) {
        }

    }

    //@Test
    public void test() throws Throwable {
        Benchmark benchmark = new BenchmarkImpl();
        benchmark.setRunMap(new RunMapImpl());
        DemoTest demoTest = new DemoTest();

        benchmark.measure(demoTest, "Test", new MyClass()).method("Andrei", 10L, 1);
        benchmark.measure(demoTest, "Test", new MyClass()).method("Andrei", 10L, 1, 2, 3);
        benchmark.measure(demoTest, "Test", new MyClass()).method("Andrei", 10L);
        benchmark.measure(demoTest, "Test", new MyClass()).method("Andrei");
        benchmark.measure(demoTest, "Test", new MyClass()).method("Andrei", 10);
        benchmark.measure(demoTest, "Test", new MyClass()).method(1, 2, 3);
        benchmark.measure(demoTest, "Test", new MyClass()).method();
    }
}