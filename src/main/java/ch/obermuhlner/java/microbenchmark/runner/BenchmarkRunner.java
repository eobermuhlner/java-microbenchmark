package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.printer.CsvResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.SimpleResultPrinter;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkConfig;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkRunnerOneArgument;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkRunner {

    private BenchmarkConfig config = new BenchmarkConfig();

    public BenchmarkRunner verbose(boolean verbose) {
        config.simpleResultPrinter.setVerbose(verbose);
        return this;
    }

    public BenchmarkRunner measureFirstTimeOnly(boolean measureFirstTimeOnly) {
        config.measureFirstTimeOnly = measureFirstTimeOnly;
        return this;
    }

    public BenchmarkRunner allocatedMeasureSeconds(double allocatedSeconds) {
        config.allocatedMeasureSeconds = allocatedSeconds;
        return this;
    }

    public BenchmarkRunner allocatedWarmupSeconds(double allocatedWarmupSeconds) {
        config.allocatedWarmupSeconds = allocatedWarmupSeconds;
        return this;
    }

    public BenchmarkRunner warmupCount(int warmupCount) {
        return minWarmupCount(warmupCount).maxWarmupCount(warmupCount);
    }

    public BenchmarkRunner minWarmupCount(int minWarmupCount) {
        config.minWarmupCount = minWarmupCount;
        return this;
    }

    public BenchmarkRunner maxWarmupCount(int maxWarmupCount) {
        config.maxWarmupCount = maxWarmupCount;
        return this;
    }

    public BenchmarkRunner measureCount(int measureCount) {
        return minMeasureCount(measureCount).maxMeasureCount(measureCount);
    }

    public BenchmarkRunner minMeasureCount(int minMeasureCount) {
        config.minMeasureCount = minMeasureCount;
        return this;
    }

    public BenchmarkRunner maxMeasureCount(int maxMeasureCount) {
        config.maxMeasureCount = maxMeasureCount;
        return this;
    }

    public BenchmarkRunner runCount(int runCount) {
        config.runCount = runCount;
        return this;
    }

    public BenchmarkRunner timeoutSeconds(long timeoutSeconds) {
        config.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public BenchmarkRunner csvReport(String fileName) {
        try {
            CsvResultPrinter csvResultPrinter = new CsvResultPrinter(new PrintWriter(new BufferedWriter(new FileWriter(fileName))));
            config.resultPrinter.addResultPrinter(csvResultPrinter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkRunner printReport(String fileName) {
        try {
            config.resultPrinter.addResultPrinter(new SimpleResultPrinter(new PrintStream(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkRunner timeUnit(TimeUnit timeUnit) {
        config.timeUnit = timeUnit;
        return this;
    }

    public BenchmarkRunnerOneArgument<Integer> forLoop(int startValue, int exclEndValue) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, Function.identity());
    }

    public <T> BenchmarkRunnerOneArgument<T> forLoop(int startValue, int exclEndValue, Function<Integer, T> converter) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, converter);
    }

    public BenchmarkRunnerOneArgument<Integer> forLoop(int startValue, int exclEndValue, int step) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, Function.identity());
    }

    public <T> BenchmarkRunnerOneArgument<T> forLoop(int startValue, int exclEndValue, int step, Function<Integer, T> converter) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, converter);
    }

    public <T> BenchmarkRunnerOneArgument<T> forLoop(T startValue, Predicate<T> condition, Function<T, T> stepFunction) {
        return forLoop(startValue, condition, stepFunction, Function.identity());
    }

    public <A, T> BenchmarkRunnerOneArgument<T> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T> converter) {
        return forLoop(startValue, condition, stepFunction, converter, String::valueOf);
    }

    public <A, T> BenchmarkRunnerOneArgument<T> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T> converter, Function<A, String> argumentToNameFunction) {
        List<T> arguments = new ArrayList<>();
        List<String> argumentsNames = new ArrayList<>();

        A value = startValue;
        while (condition.test(value)) {
            arguments.add(converter.apply(value));
            argumentsNames.add(argumentToNameFunction.apply(value));
            value = stepFunction.apply(value);
        }

        return forArguments(arguments, argumentsNames);
    }

    public <T> BenchmarkRunnerOneArgument<T> forArguments(Stream<T> stream) {
        return forArguments(stream.collect(Collectors.toList()));
    }

    public <T> BenchmarkRunnerOneArgument<T> forArguments(T... arguments) {
        return forArguments(Arrays.asList(arguments));
    }

    public <T> BenchmarkRunnerOneArgument<T> forArguments(List<T> arguments) {
        return forArguments(arguments, String::valueOf);
    }

    public <T> BenchmarkRunnerOneArgument<T> forArguments(List<T> arguments, Function<T, String> argumentToNameFunction) {
        List<String> argumentsNames = arguments.stream().map(argumentToNameFunction).collect(Collectors.toList());
        return forArguments(arguments, argumentsNames);
    }

    public <T> BenchmarkRunnerOneArgument<T> forArguments(List<T> arguments, List<String> argumentsNames) {
        return new BenchmarkRunnerOneArgument<T>(config, arguments, argumentsNames);
    }
}
