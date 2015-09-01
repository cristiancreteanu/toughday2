package com.day.qa.toughday.tests;

import com.adobe.granite.testing.ClientException;
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
