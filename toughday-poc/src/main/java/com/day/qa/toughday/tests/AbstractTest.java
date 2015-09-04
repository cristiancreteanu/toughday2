package com.day.qa.toughday.tests;

import com.day.qa.toughday.AbstractTestRunner;
import com.day.qa.toughday.cli.CliArg;

import java.util.UUID;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    private UUID id;
    private String name;

    AbstractTest() {
        this.id = UUID.randomUUID();
    }

    public String getName() {
        return name != null ? name : getClass().getSimpleName();
    }

    @CliArg(required = false)
    public void setName(String name) {
        this.name = name;
    }


    public final UUID getId() {
        return id;
    }

    public final void setID(UUID id) {
        this.id = id;
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        if(!(other instanceof AbstractTest)) {
            return false;
        }
        return ((AbstractTest)other).getId().equals(id);
    }

    public abstract Class<? extends AbstractTestRunner> getTestRunnerClass();
    public abstract AbstractTest newInstance();
}
