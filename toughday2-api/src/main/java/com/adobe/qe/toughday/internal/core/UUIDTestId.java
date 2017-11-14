package com.adobe.qe.toughday.internal.core;

import com.adobe.qe.toughday.api.core.TestId;

import java.util.UUID;

/**
 * Created by tuicu on 18/10/17.
 */
public class UUIDTestId extends TestId {
    private UUID id;

    public UUIDTestId() {
        this(UUID.randomUUID());
    }

    public UUIDTestId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(TestId testId) {
        if(!(testId instanceof UUIDTestId))
            return false;

        return this.id.equals(((UUIDTestId) testId).id);
    }

    @Override
    public long getHashCode() {
        return id.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
