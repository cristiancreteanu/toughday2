package com.adobe.qe.toughday.metrics;

/**
 * A class that contains all the information that publishers need in order to display this metric.
 * @param <T>
 */
public abstract class ResultInfo<T> {
    //we need a refrence to a metric in order to be able to access the configurable fields of the class.
    private Metric metric;

    public ResultInfo(Metric metric) {
        this.metric = metric;
    }

    public abstract String getFormat();

    /**
     * The unit of measure for the result of this metric.
     */
    public abstract String getUnitOfMeasure();

    /**
     * The value of this metric.
     */
    public abstract T getValue();

    /**
     * The name of the metric.
     * @return
     */
    public String getName() {
        return metric.getName();
    }

    public int getDecimals() { return metric.getDecimals();}
}
