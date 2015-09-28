package com.day.qa.toughday.core;

/**
 * Created by tuicu on 10/09/15.
 * Exception for failing composite tests in case a child test fails.
 */
public class ChildTestFailedException extends Exception {
    public ChildTestFailedException(Exception e) {
        super(e);
    }
}
