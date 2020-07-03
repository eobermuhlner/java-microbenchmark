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

    public double allocatedMeasureSeconds = 1.0;
    public double allocatedWarmupSeconds = 0.1;
    public int minWarmupCount = 1;
    public int maxWarmupCount = 1_000_000_000;
    public int minMeasureCount = 1;
    public int maxMeasureCount = 1_000_000_000;
    public long timeoutSeconds = 60;
    public boolean measureFirstTimeOnly = false;

    public int runCount = 10;

    public List<String> names = new ArrayList<>();
    public TimeUnit timeUnit = TimeUnit.NanoSeconds;

    public Function<double[], Double> resultCalculator = ResultCalculators.AVERAGE_LOWER_HALF;

    public final CompositeResultPrinter resultPrinter;
    public final SimpleResultPrinter simpleResultPrinter = new SimpleResultPrinter();
    public CsvResultPrinter csvResultPrinter;

    public BenchmarkConfig() {
        resultPrinter = new CompositeResultPrinter(simpleResultPrinter);
    }
}
