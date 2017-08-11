package com.adobe.qe.toughday.metrics;

/**
 * A class that contains all the information that publishers need in order to display this metric.
 * @param <T>
 */
public interface ResultInfo<T> {
    String getFormat();

    /**
     * The unit of measure for the result of this metric.
     */
    String getUnitOfMeasure();

    /**
     * The value of this metric.
     */
    T getValue();

    /**
     * The name of the metric.
     * @return
     */

    String getName();

    int getDecimals();

}
