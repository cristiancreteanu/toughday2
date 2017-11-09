package com.adobe.qe.toughday.api.core;

/**
 * Exception for failing composite tests in case a child test fails.
 */
public class ChildTestFailedException extends ToughDayException {
    public ChildTestFailedException(String message, Throwable e) { super(message, e);}

    public ChildTestFailedException(Throwable e) {
        super(e.getMessage(), e);
    }
}
