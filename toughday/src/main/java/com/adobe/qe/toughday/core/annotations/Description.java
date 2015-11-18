package com.adobe.qe.toughday.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Description {
    /**
     * The name of the test class
     * @return
     */
    String name();

    /**
     * The text description of the test class
     * @return
     */
    String desc();
}
