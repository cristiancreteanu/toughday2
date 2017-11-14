package com.adobe.qe.toughday.api.core;

public interface MetricResult<T> {

    /**
     * Getter for the name of the metric.
     * @return
     */
    String getName();

    /**
     * Getter for the value of this metric.
     */
    T getValue();


    /**
     * Getter for the format of the result of this metric. For instance, if this metric is going to return a string as
     * a result, this method should return "%s".
     */
    String getFormat();

    /**
     * Getter fot the unit of measure for the value of this metric.
     */
    String getUnitOfMeasure();


}
