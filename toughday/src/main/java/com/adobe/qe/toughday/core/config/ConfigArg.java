package com.adobe.qe.toughday.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a setter that received a String parameter to expose it as a configuration property.
 * These properties will be automatically picked up, shown in help and the they will be called by Configuration
 * when the object is instantiated. Supported classes: subtypes of AbstractTest, classes that implement Publisher and
 * GlobalArgs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ConfigArg {
    boolean required() default true;
    String desc() default "";
    String defaultValue() default "";
}
