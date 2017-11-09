package com.adobe.qe.toughday.internal.core.metrics;

import com.adobe.qe.toughday.api.core.MetricResult;

public class MetricResultImpl<T> implements MetricResult {

    private String name;
    private T value;
    private String format;
    private String unitOfMeasure;

    public MetricResultImpl(String name, T value, String format, String unitOfMeasure) {
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
