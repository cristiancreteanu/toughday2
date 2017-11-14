package com.adobe.qe.toughday.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for executing a method once before any runs of any replication (clone) of a test.
 * Guaranteed to be executed only once, no matter how many threads have a replica of the test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface CloneSetup {

}
