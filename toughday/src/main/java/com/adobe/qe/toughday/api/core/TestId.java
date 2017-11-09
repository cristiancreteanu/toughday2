package com.adobe.qe.toughday.api.core;

/**
 * Identifies a test in the suite and all its clones
 */
public abstract class TestId {
    /**
     * Mandatory equals method.
     * @param testId the other TestId instance
     * @return true if they are equals. false otherwise
     */
    public abstract boolean equals(TestId testId);

    /**
     * Mandatory method for computing the hash code
     * @return the hash code
     */
    public abstract long getHashCode();

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof TestId)) return false;
        return equals((TestId) other);
    }

    @Override
    public int hashCode() {
        return (int) getHashCode();
    }
}
