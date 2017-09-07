package com.adobe.qe.toughday.metrics;

public interface MetricResult<T> {

    /**
     * The name of the metric.
     * @return
     */
    String getName();

    /**
     * The value of this metric.
     */
    T getValue();


    String getFormat();

    /**
     * The unit of measure for the value of this metric.
     */
    String getUnitOfMeasure();


}
