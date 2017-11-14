package com.adobe.qe.toughday.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a getter to expose it as a configuration property.
 * These properties will be automatically picked up, shown in logging or in "dry" runmode
 * Supported classes: subtypes of AbstractTest, subtypes of Publisher and
 * GlobalArgs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ConfigArgGet {
    String name() default "";
}
