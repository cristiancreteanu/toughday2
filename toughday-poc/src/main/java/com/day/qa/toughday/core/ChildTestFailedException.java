package com.day.qa.toughday.core;

/**
 * Created by tuicu on 10/09/15.
 */
public class ChildTestFailedException extends Exception {
    public ChildTestFailedException(Exception e) {
        super(e);
    }
}
