package com.adobe.qe.toughday.metrics;

import com.adobe.qe.toughday.core.RunMap;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * Base class for all metrics. Classes extending this class, if not abstract, will be shown in help.
 */
public abstract class Metric {

    protected String name;
    protected int decimals;

    private static final int DEFAULT_DECIMALS = 6;

    public static final List<Metric> basicMetrics = Collections.unmodifiableList(
            new ArrayList<Metric>() {{
                add(new Passed());
                add(new Failed());
                add(new Skipped());
            }});

    public static final List<Metric> defaultMetrics = Collections.unmodifiableList(
            new ArrayList<Metric>() {{
                addAll(basicMetrics);
                add(new Average());
                add(new Median());
                add(new StdDev());
                add(new Percentile().setValue("90p"));
                add(new Percentile().setValue("99p"));
                add(new Percentile().setValue("99.9p"));
                add(new Min());
                add(new Max());
                add(new RealTP());
            }});


    public Metric() {
        this.name = getClass().getSimpleName();
        this.decimals = DEFAULT_DECIMALS;
    }

    /**
     * Returns all the information that publishers need in order to print this metric.
     * @return
     */

    public MetricResult getResult(RunMap.TestEntry testEntry) {
        return new MetricResultImp<>(this.getName(), this.getValue(testEntry), this.getFormat(), this.getUnitOfMeasure());
    }

    public abstract Object getValue(RunMap.TestEntry testEntry);

    @ConfigArgSet(required = false, desc = "The name of the metric.")
    public Metric setName(String name) {
        this.name = name;
        return this;
    }

    @ConfigArgSet(required = false, desc = "Number of decimals.", defaultValue = "6")
    public Metric setDecimals(String decimals) {
        this.decimals = Integer.parseInt(decimals);
        return this;
    }

    @ConfigArgGet
    public String getName() {
        return name;
    }

    @ConfigArgGet
    public int getDecimals() {
        return decimals;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof Metric && this.getName().equals(((Metric) o).getName()));
    }

    public abstract String getFormat();

    public abstract String getUnitOfMeasure();

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37).append(name).toHashCode();
    }
}
