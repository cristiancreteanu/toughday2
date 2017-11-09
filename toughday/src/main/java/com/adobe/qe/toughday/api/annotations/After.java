package com.adobe.qe.toughday.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for executing a method after each run of a test.
 * The method is guaranteed to be called even if the test fails and Exceptions are thrown.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface After {

}
