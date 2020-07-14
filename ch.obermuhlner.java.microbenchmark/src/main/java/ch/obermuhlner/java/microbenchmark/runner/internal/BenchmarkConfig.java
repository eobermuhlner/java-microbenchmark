package ch.obermuhlner.java.microbenchmark.runner.internal;

import ch.obermuhlner.java.microbenchmark.printer.CompositeResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.CsvResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.SimpleResultPrinter;
import ch.obermuhlner.java.microbenchmark.runner.ResultCalculators;
import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class BenchmarkConfig {

    private double allocatedWarmupRatio = 0.1;
    private double allocatedSleepRatio = 0.01;
    private double allocatedMeasureSeconds = 1.0;
    private Double allocatedWarmupSeconds = null;
    private Double allocatedSleepSeconds = null;
    private int preWarmupCount = 0;
    private int minWarmupCount = 1;
    private int maxWarmupCount = 1_000_000_000;
    private int minMeasureCount = 1;
    private int maxMeasureCount = 1_000_000_000;
    private long timeoutSeconds = 60;
    private boolean measureFirstTimeOnly = false;

    public int runCount = 10;

    public List<String> names = new ArrayList<>();
    private TimeUnit timeUnit = TimeUnit.NanoSeconds;

    public Function<double[], Double> resultCalculator = ResultCalculators.AVERAGE_LOWER_HALF;

    public final CompositeResultPrinter resultPrinter;
    public final SimpleResultPrinter simpleResultPrinter = new SimpleResultPrinter();
    private CsvResultPrinter csvResultPrinter;

    public BenchmarkConfig() {
        resultPrinter = new CompositeResultPrinter(simpleResultPrinter);
    }

    public double getAllocatedMeasureSeconds() {
        return allocatedMeasureSeconds;
    }

    public void setAllocatedMeasureSeconds(double allocatedMeasureSeconds) {
        this.allocatedMeasureSeconds = allocatedMeasureSeconds;
    }

    public double getAllocatedWarmupSeconds() {
        if (allocatedWarmupSeconds == null) {
            return allocatedMeasureSeconds * allocatedWarmupRatio;
        }
        return allocatedWarmupSeconds;
    }

    public void setAllocatedWarmupSeconds(double allocatedWarmupSeconds) {
        this.allocatedWarmupSeconds = allocatedWarmupSeconds;
    }

    public double getAllocatedSleepSeconds() {
        if (allocatedSleepSeconds == null) {
            return allocatedMeasureSeconds * allocatedSleepRatio;
        }
        return allocatedSleepSeconds;
    }

    public void setAllocatedSleepSeconds(double allocatedSleepSeconds) {
        this.allocatedSleepSeconds = allocatedSleepSeconds;
    }

    public int getPreWarmupCount() {
        return preWarmupCount;
    }

    public void setPreWarmupCount(int preWarmupCount) {
        this.preWarmupCount = preWarmupCount;
    }

    public int getMinWarmupCount() {
        return minWarmupCount;
    }

    public void setMinWarmupCount(int minWarmupCount) {
        this.minWarmupCount = minWarmupCount;
    }

    public int getMaxWarmupCount() {
        return maxWarmupCount;
    }

    public void setMaxWarmupCount(int maxWarmupCount) {
        this.maxWarmupCount = maxWarmupCount;
    }

    public int getMinMeasureCount() {
        return minMeasureCount;
    }

    public void setMinMeasureCount(int minMeasureCount) {
        this.minMeasureCount = minMeasureCount;
    }

    public int getMaxMeasureCount() {
        return maxMeasureCount;
    }

    public void setMaxMeasureCount(int maxMeasureCount) {
        this.maxMeasureCount = maxMeasureCount;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isMeasureFirstTimeOnly() {
        return measureFirstTimeOnly;
    }

    public void setMeasureFirstTimeOnly(boolean measureFirstTimeOnly) {
        this.measureFirstTimeOnly = measureFirstTimeOnly;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Function<double[], Double> getResultCalculator() {
        return resultCalculator;
    }

    public void setResultCalculator(Function<double[], Double> resultCalculator) {
        this.resultCalculator = resultCalculator;
    }

    public CompositeResultPrinter getResultPrinter() {
        return resultPrinter;
    }

    public SimpleResultPrinter getSimpleResultPrinter() {
        return simpleResultPrinter;
    }

    public CsvResultPrinter getCsvResultPrinter() {
        return csvResultPrinter;
    }

    public void setCsvResultPrinter(CsvResultPrinter csvResultPrinter) {
        this.csvResultPrinter = csvResultPrinter;
    }
}
