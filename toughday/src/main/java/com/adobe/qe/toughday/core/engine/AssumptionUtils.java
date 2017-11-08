package com.adobe.qe.toughday.core.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class with helpers to validate assumptions about the extensions
 */
public final class AssumptionUtils {
    /**
     * Validator for methods annotated with test_annotations.
     * @param method
     * @param annotation
     * @return true if the method is valid, false if not.
     */
    public static void validateAnnotatedMethod(Method method, Class<? extends Annotation> annotation) {
        if(method.getParameterTypes().length != 0) {
            throw new AssertionError("Method \"" + method + "\" annotated with " + annotation.getSimpleName() + " cannot have parameters");
        }

        if(!(Modifier.isFinal(method.getModifiers()) || Modifier.isPrivate(method.getModifiers()))) {
            throw new AssertionError("Method \"" + method + "\" annotated with " + annotation.getSimpleName() + " must be final or private");
        }
    }
}
