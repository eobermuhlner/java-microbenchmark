package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.printer.CsvResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.SimpleResultPrinter;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkConfig;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkRunnerOneArgument;
import ch.obermuhlner.java.microbenchmark.runner.internal.SimpleBenchmarkRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkBuilder {

    private BenchmarkConfig config = new BenchmarkConfig();

    public BenchmarkBuilder verbose(boolean verbose) {
        config.simpleResultPrinter.setVerbose(verbose);
        return this;
    }

    public BenchmarkBuilder measureFirstTimeOnly(boolean measureFirstTimeOnly) {
        config.measureFirstTimeOnly = measureFirstTimeOnly;
        return this;
    }

    public BenchmarkBuilder allocatedMeasureSeconds(double allocatedSeconds) {
        config.allocatedMeasureSeconds = allocatedSeconds;
        return this;
    }

    public BenchmarkBuilder allocatedWarmupSeconds(double allocatedWarmupSeconds) {
        config.allocatedWarmupSeconds = allocatedWarmupSeconds;
        return this;
    }

    public BenchmarkBuilder warmupCount(int warmupCount) {
        return minWarmupCount(warmupCount).maxWarmupCount(warmupCount);
    }

    public BenchmarkBuilder minWarmupCount(int minWarmupCount) {
        config.minWarmupCount = minWarmupCount;
        return this;
    }

    public BenchmarkBuilder maxWarmupCount(int maxWarmupCount) {
        config.maxWarmupCount = maxWarmupCount;
        return this;
    }

    public BenchmarkBuilder measureCount(int measureCount) {
        return minMeasureCount(measureCount).maxMeasureCount(measureCount);
    }

    public BenchmarkBuilder minMeasureCount(int minMeasureCount) {
        config.minMeasureCount = minMeasureCount;
        return this;
    }

    public BenchmarkBuilder maxMeasureCount(int maxMeasureCount) {
        config.maxMeasureCount = maxMeasureCount;
        return this;
    }

    public BenchmarkBuilder runCount(int runCount) {
        config.runCount = runCount;
        return this;
    }

    public BenchmarkBuilder timeoutSeconds(long timeoutSeconds) {
        config.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public BenchmarkBuilder resultCalculator(Function<double[], Double> resultCalculator) {
        config.resultCalculator = resultCalculator;
        return this;
    }

    public BenchmarkBuilder csvReport(String fileName) {
        try {
            CsvResultPrinter csvResultPrinter = new CsvResultPrinter(new PrintWriter(new BufferedWriter(new FileWriter(fileName))));
            config.resultPrinter.addResultPrinter(csvResultPrinter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkBuilder printReport(String fileName) {
        try {
            config.resultPrinter.addResultPrinter(new SimpleResultPrinter(new PrintStream(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkBuilder timeUnit(TimeUnit timeUnit) {
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

    public double measure(Runnable snippet) {
        return new SimpleBenchmarkRunner(config)
                .measure(snippet);
    }

    public <T> double measure(Consumer<T> snippet, T argument) {
        return new SimpleBenchmarkRunner(config)
                .measure(() -> snippet.accept(argument));
    }

    public <T1, T2> double measure(BiConsumer<T1, T2> snippet, T1 argument1, T2 argument2) {
        return new SimpleBenchmarkRunner(config)
                .measure(() -> snippet.accept(argument1, argument2));
    }
}
