package com.adobe.qe.toughday.metrics;

public class MetricResultImp<T> implements MetricResult {

    private String name;
    private T value;
    private String format;
    private String unitOfMeasure;

    public MetricResultImp(String name, T value, String format, String unitOfMeasure) {
        this.value = value;
        this.format = format;
        this.unitOfMeasure = unitOfMeasure;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

}
