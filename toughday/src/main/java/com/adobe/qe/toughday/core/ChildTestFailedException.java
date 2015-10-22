package com.adobe.qe.toughday.core;

/**
 * Exception for failing composite tests in case a child test fails.
 */
public class ChildTestFailedException extends Exception {
    public ChildTestFailedException(Exception e) {
        super(e);
    }
}
