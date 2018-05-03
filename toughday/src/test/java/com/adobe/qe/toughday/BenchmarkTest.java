/*
Copyright 2015 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/
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
