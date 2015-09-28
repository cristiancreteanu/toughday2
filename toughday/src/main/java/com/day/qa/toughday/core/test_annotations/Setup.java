package com.day.qa.toughday.core.test_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tuicu on 20/08/15.
 * Annotation for executing a method once before any runs of any replication (clone) of a test.
 * Guaranteed to be executed only once, no matter how many threads have a replica of the test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Setup {

}
