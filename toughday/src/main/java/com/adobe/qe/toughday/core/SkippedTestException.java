package com.adobe.qe.toughday.core;

/**
 * Exception for skipping tests in case a dependency test failed/was not executed.
 */
public class SkippedTestException extends ToughDayException {
    public SkippedTestException(Throwable e) {
        super(e.getMessage(), e);
    }

    public SkippedTestException(String message, Throwable e) {
        super(message, e);
    }
}
