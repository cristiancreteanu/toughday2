package com.adobe.qe.toughday.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Name {
    /**
     * The name of the test class
     * @return
     */
    String name();
}
