package com.day.qa.toughday.tests;

import com.adobe.granite.testing.ClientException;
import com.day.qa.toughday.tests.annotations.After;
import com.day.qa.toughday.tests.annotations.Before;
import com.day.qa.toughday.tests.annotations.Setup;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    private boolean setupExecuted;
    private Method setupMethod;
    private Method beforeMethod;
    private Method afterMethod;
    private UUID id;

    AbstractTest() {
        this.id = UUID.randomUUID();
        setupExecuted = true;
        for(Method method : this.getClass().getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if(annotation.annotationType() == Setup.class) {
                    if(validateMethod(method, Setup.class)) {
                        setupMethod = method;
                        setupExecuted = false;
                    }
                }
                else if (annotation.annotationType() == Before.class) {
                    if(validateMethod(method, Before.class)) {
                        beforeMethod = method;
                    }
                }
                else if (annotation.annotationType() == After.class) {
                    if(validateMethod(method, After.class)) {
                        afterMethod = method;
                    }
                }
            }
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }
    public long runTest() throws ClientException {
        if(!setupExecuted) {
            synchronized (this) {
                if(!setupExecuted) {
                    setupExecuted = true;
                    executeMethod(setupMethod, Setup.class);
                }
            }
        }
        if(beforeMethod != null) {
            executeMethod(beforeMethod, Before.class);
        }
        Long start = System.nanoTime();
        test();
        Long elapsed = (System.nanoTime() - start) / 1000000l;
        if(afterMethod != null) {
            executeMethod(afterMethod, After.class);
        }
        return elapsed;
    }

    private void executeMethod(Method method, Class<? extends Annotation> annotation) {
        try {
            method.invoke(this);
        } catch (IllegalAccessException e) {
            System.out.println("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName() + ": Illegal Access.");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("Could not execute " + method.getName() +
                    " annotated with " + annotation.getSimpleName());
            e.printStackTrace();
        }
    }

    private boolean validateMethod(Method method, Class<? extends Annotation> annotation) {
        if(method.getParameterTypes().length != 0) {
            System.out.println("Method " + method + " annotated with " + annotation.getSimpleName() + " cannot have parameters");
            return false;
        }
        return true;
    }

    public UUID getId() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof AbstractTest)) {
            return false;
        }
        return ((AbstractTest)other).getId().equals(id);
    }

    public abstract void test() throws ClientException;
    public abstract AbstractTest newInstance();
}
