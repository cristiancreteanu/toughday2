package com.adobe.qe.toughday.core;

/**
 * Generic ToughDay exception.
 */
public class ToughDayException extends Exception {
    public ToughDayException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ToughDayException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }
}
