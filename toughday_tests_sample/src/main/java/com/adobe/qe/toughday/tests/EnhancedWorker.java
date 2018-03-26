package com.adobe.qe.toughday.tests;

public class EnhancedWorker extends Worker {
    public long muchWork(long millis) throws Throwable {
        doWork(millis);
        System.out.println("I am exhausted!");
        return millis;
    }
}
