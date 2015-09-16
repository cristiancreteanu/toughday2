package com.day.qa.toughday.core;

import com.day.qa.toughday.core.cli.CliArg;

import java.util.List;
import java.util.UUID;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    private UUID id;
    private String name;
    private AbstractTest parent;

    public AbstractTest() {
        this.id = UUID.randomUUID();
    }

    public String getSimpleName() {
        return name != null ? name : getClass().getSimpleName();
    }

    public String getName() {
        return parent != null ? parent.getName() + "." + getSimpleName() : getSimpleName();
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

    public AbstractTest getParent() {
        return parent;
    }

    public void setParent(AbstractTest parent) {
        this.parent = parent;
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

    public AbstractTest clone() {
        AbstractTest newInstance = newInstance();
        newInstance.setID(this.id);
        newInstance.setName(this.getSimpleName());
        return newInstance;
    }

    public abstract List<AbstractTest> getChildren();
    public abstract Class<? extends AbstractTestRunner> getTestRunnerClass();
    public abstract AbstractTest newInstance();
}
