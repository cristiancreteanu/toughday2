package com.adobe.qe.toughday.core;

/**
 * Exception for skipping tests in case a dependency test failed/was not executed.
 */
public class SkippedTestException extends Exception {
    public SkippedTestException(Exception e) {
        super(e);
    }
}
