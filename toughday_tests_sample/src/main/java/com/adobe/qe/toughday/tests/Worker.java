package com.adobe.qe.toughday.tests;

public class Worker {
    public long doWork() throws Throwable { return doWork(40); }
    public long doWork(long millis) throws Throwable { Thread.sleep(millis); return millis; }
}