package com.day.qa.toughday.tests;

import com.adobe.granite.testing.ClientException;

import java.util.UUID;

/**
 * Created by tuicu on 12/08/15.
 */
public abstract class AbstractTest {
    private UUID id;

    AbstractTest() {
        this.id = UUID.randomUUID();
    }

    public String getName() {
        return getClass().getSimpleName();
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
