package com.day.qa.toughday;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by tuicu on 12/08/15.
 */
public class TestSuite {
    private static class TestEntry {
        int weight;
        double duration;
        long runs;

        public TestEntry(int weight) {
            this.weight = weight;
        }

        public synchronized void incrementRuns() {
            runs++;
        }

        public synchronized void addDuration(double elapsed) {
            duration += elapsed;
        }

        public long getRuns() {
            return runs;
        }

        public double getDuration() {
            return duration;
        }

        public int getWeight() {
            return weight;
        }
    }

    private static Random _rnd = new Random();

    private AbstractTest getNextTest(List<AbstractTest> tests, int totalWeight) {
        int randomNumber = _rnd.nextInt(totalWeight);

        AbstractTest selectedTest = null;
        for (AbstractTest test : tests) {
            int testWeight = runsMap.get(test).getWeight();
            if (randomNumber < testWeight) {
                selectedTest = test;
                break;
            }
            randomNumber = randomNumber - testWeight;
        }

        return selectedTest;
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private int duration;
    private List<AbstractTest> testSuite;
    private int totalWeight;
    private int delay;
    private int concurrency;
    private ExecutorService executorService;

    HashMap<AbstractTest, TestEntry> runsMap;

    public TestSuite(int durationSec, int delay, int concurrency) {
        this.testSuite = new ArrayList<>();
        this.delay = delay;
        this.runsMap = new HashMap<>();
        this.concurrency = concurrency;
        this.executorService = Executors.newFixedThreadPool(concurrency);
        this.duration = durationSec;
    }

    public TestSuite add(AbstractTest test, int weight) {
        testSuite.add(test);
        totalWeight += weight;
        runsMap.put(test, new TestEntry(weight));
        return this;
    }
    
    public void test() {
        List<AsyncTestRun> tests = new ArrayList<>();
        for(int i = 0; i < concurrency; i++) {
            AsyncTestRun runner = new AsyncTestRun();
            tests.add(runner);
            executorService.execute(runner);
        }
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            for(AsyncTestRun run : tests)
                run.finishExecution();
        }
        shutdownAndAwaitTermination(executorService);
        for(AbstractTest test : runsMap.keySet()) {
            TestEntry entry = runsMap.get(test);
            System.out.println("Weight: " + entry.getWeight() + " Runned: " + entry.getRuns()
                    + " Throughput: " + entry.getRuns() / entry.getDuration());
        }
    }

    private class AsyncTestRun implements Runnable {
        private boolean finish = false;

        public void finishExecution() {
            finish = true;
        }
        public void run() {
            try {
                while (true) {
                    if (finish) return;
                    AbstractTest nextTest = getNextTest(testSuite, totalWeight);
                    Long nanoSecElapsed = nextTest.runTest();
                    TestEntry entry = runsMap.get(nextTest);
                    entry.incrementRuns();
                    entry.addDuration(nanoSecElapsed / 1000000000.0);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
