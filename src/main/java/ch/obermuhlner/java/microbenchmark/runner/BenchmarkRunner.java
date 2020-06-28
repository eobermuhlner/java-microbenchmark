package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.printer.CompositeResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.CsvResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.SimpleResultPrinter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkRunner<T> {

    private static final double NANOS_PER_SECOND = 1_000_000_000;

    private final CompositeResultPrinter resultPrinter;

    private double allocatedMeasureSeconds = 1.0;
    private double allocatedWarmupSeconds = 0.1;
    private int minWarmupCount = 1;
    private int maxWarmupCount = 1_000_000_000;
    private int minMeasureCount = 1;
    private int maxMeasureCount = 1_000_000_000;
    private long timeoutSeconds = 10;
    private boolean measureFirstTimeOnly = false;

    private int runCount = 10;

    private List<T> arguments1;
    private List<T> arguments2;
    private List<String> names = new ArrayList<>();
    private List<String> arguments1Names = new ArrayList<>();
    private List<String> arguments2Names = new ArrayList<>();
    private List<Consumer<T>> benchmarkSnippets1 = new ArrayList<>();
    private List<BiConsumer<T, T>> benchmarkSnippets2 = new ArrayList<>();
    private Function<double[], Double> resultStrategy = ResultStrategies.AVERAGE_LOWER_HALF;

    public BenchmarkRunner() {
        resultPrinter = new CompositeResultPrinter(new SimpleResultPrinter());
    }

    public BenchmarkRunner<T> measureFirstTimeOnly(boolean measureFirstTimeOnly) {
        this.measureFirstTimeOnly = measureFirstTimeOnly;
        return this;
    }

    public BenchmarkRunner<T> allocatedMeasureSeconds(double allocatedSeconds) {
        this.allocatedMeasureSeconds = allocatedSeconds;
        return this;
    }

    public BenchmarkRunner<T> allocatedWarmupSeconds(double allocatedWarmupSeconds) {
        this.allocatedWarmupSeconds = allocatedWarmupSeconds;
        return this;
    }

    public BenchmarkRunner<T> warmupCount(int warmupCount) {
        return minWarmupCount(warmupCount).maxWarmupCount(warmupCount);
    }

    public BenchmarkRunner<T> minWarmupCount(int minWarmupCount) {
        this.minWarmupCount = minWarmupCount;
        return this;
    }

    public BenchmarkRunner<T> maxWarmupCount(int maxWarmupCount) {
        this.maxWarmupCount = maxWarmupCount;
        return this;
    }

    public BenchmarkRunner<T> measureCount(int measureCount) {
        return minMeasureCount(measureCount).maxMeasureCount(measureCount);
    }

    public BenchmarkRunner<T> minMeasureCount(int minMeasureCount) {
        this.minMeasureCount = minMeasureCount;
        return this;
    }

    public BenchmarkRunner<T> maxMeasureCount(int maxMeasureCount) {
        this.maxMeasureCount = maxMeasureCount;
        return this;
    }

    public BenchmarkRunner<T> runCount(int runCount) {
        this.runCount = runCount;
        return this;
    }

    public BenchmarkRunner<T> timeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public BenchmarkRunner<T> csvReport(String fileName) {
        try {
            resultPrinter.addResultPrinter(new CsvResultPrinter(new PrintWriter(new BufferedWriter(new FileWriter(fileName)))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkRunner<T> printReport(String fileName) {
        try {
            resultPrinter.addResultPrinter(new SimpleResultPrinter(new PrintStream(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkRunner<T> forLoop(int startValue, int exclEndValue, Function<Integer, T> converter) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, converter);
    }

    public BenchmarkRunner<T> forLoop(int startValue, int exclEndValue, int step, Function<Integer, T> converter) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, converter);
    }

    public BenchmarkRunner<T> forLoop(T startValue, Predicate<T> condition, Function<T, T> stepFunction) {
        return forLoop(startValue, condition, stepFunction, t -> t);
    }

    public <A> BenchmarkRunner<T> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T> converter) {
        List<T> arguments = new ArrayList<>();
        List<String> argumentsNames = new ArrayList<>();

        A value = startValue;
        while (condition.test(value)) {
            arguments.add(converter.apply(value));
            argumentsNames.add(String.valueOf(value));
            value = stepFunction.apply(value);
        }

        return forArguments(arguments, argumentsNames);
    }

    public BenchmarkRunner<T> forStream(Stream<T> stream) {
        return forArguments(stream.collect(Collectors.toList()));
    }

    public BenchmarkRunner<T> forArguments(T... arguments) {
        return forArguments(Arrays.asList(arguments));
    }

    public BenchmarkRunner<T> forArguments(List<T> arguments) {
        List<String> argumentsNames = arguments.stream().map(String::valueOf).collect(Collectors.toList());
        return forArguments(arguments, argumentsNames);
    }

    public BenchmarkRunner<T> forArguments(List<T> arguments, List<String> argumentsNames) {
        if (arguments1 == null) {
            arguments1 = arguments;
            arguments1Names = argumentsNames;
            return this;
        }
        if (arguments2 == null) {
            arguments2 = arguments;
            arguments2Names = argumentsNames;
            return this;
        }
        throw new RuntimeException("Too many argument dimensions");
    }

    public BenchmarkRunner<T> benchmark(String name, Consumer<T> snippet) {
        names.add(name);
        benchmarkSnippets1.add(snippet);
        return this;
    }

    public BenchmarkRunner<T> benchmark(String name, BiConsumer<T, T> snippet) {
        names.add(name);
        benchmarkSnippets2.add(snippet);
        return this;
    }

    public void run() {
        try {
            if (arguments2 != null) {
                runSnippets2();
            } else {
                runSnippets1();
            }
        } finally {
            resultPrinter.close();
        }
    }

    private void runSnippets1() {
        resultPrinter.printDimensions(1);
        resultPrinter.printNames(names);
        resultPrinter.printArguments(arguments1Names);

        WarmupInfo[] warmupInfos = new WarmupInfo[names.size() * arguments1.size()];

        for (int i = 0; i < names.size(); i++) {
            Consumer<T> snippet = benchmarkSnippets1.get(i);
            for (int j = 0; j < arguments1.size(); j++) {
                T argument = arguments1.get(j);
                warmupInfos[i+j*names.size()] = warmup(snippet, argument);
            }
        }

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Consumer<T> snippet = benchmarkSnippets1.get(i);
            for (int j = 0; j < arguments1.size(); j++) {
                T argument = arguments1.get(j);
                String argumentName = arguments1Names.get(j);
                WarmupInfo warmupInfo = warmupInfos[i+j*names.size()];
                double[] results = measure(snippet, argument, warmupInfo.warmupCount, warmupInfo.warmupTime);
                double result = resultStrategy.apply(results);
                resultPrinter.printBenchmark(name, argumentName, result, results);
            }
        }

        resultPrinter.printFinished();
    }

    private void runSnippets2() {
        if (names.size() != 1) {
            throw new RuntimeException("Can only run exactly 1 two-dimensional benchmark");
        }

        resultPrinter.printDimensions(2);
        resultPrinter.printNames(arguments1Names);
        resultPrinter.printArguments(arguments2Names);

        BiConsumer<T, T> snippet = benchmarkSnippets2.get(0);

        WarmupInfo[] warmupInfos = new WarmupInfo[names.size() * arguments1.size()];

        for (int i = 0; i < arguments1.size(); i++) {
            T argument1 = arguments1.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T argument2 = arguments2.get(j);
                warmupInfos[i+j*names.size()] = warmup(snippet, argument1, argument2);
            }
        }

        for (int i = 0; i < arguments1.size(); i++) {
            T argument1 = arguments1.get(i);
            String argument1Name = arguments1Names.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T argument2 = arguments2.get(j);
                String argument2Name = arguments2Names.get(j);

                WarmupInfo warmupInfo = warmupInfos[i+j*names.size()];
                double[] results = measure(snippet, argument1, argument2, warmupInfo.warmupCount, warmupInfo.warmupTime);
                double result = resultStrategy.apply(results);
                resultPrinter.printBenchmark(argument1Name, argument2Name, result, results);
            }
        }

        resultPrinter.printFinished();
    }

    private WarmupInfo warmup(Consumer<T> snippet, T argument) {
        return warmup(() -> snippet.accept(argument));
    }

    private double[] measure(Consumer<T> snippet, T argument, int warmupCount, double warmupTime) {
        return measure(() -> snippet.accept(argument), warmupCount, warmupTime);
    }

    private WarmupInfo warmup(BiConsumer<T, T> snippet, T argument1, T argument2) {
        return warmup(() -> snippet.accept(argument1, argument2));
    }

    private double[] measure(BiConsumer<T, T> snippet, T argument1, T argument2, int warmupCount, double warmupTime) {
        return measure(() -> snippet.accept(argument1, argument2), warmupCount, warmupTime);
    }

    private WarmupInfo warmup(Runnable snippet) {
        double firstTime = measureWithTimeout(snippet, 1);
        if (measureFirstTimeOnly || firstTime >= allocatedMeasureSeconds * NANOS_PER_SECOND) {
            return new WarmupInfo(1, firstTime);
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < allocatedWarmupSeconds * NANOS_PER_SECOND && warmupCount >= minWarmupCount && warmupCount < maxWarmupCount) {
            double warmupTime = measure(snippet, 1);
            warmupSpentTime += warmupTime;
            warmupCount++;
        }

        double warmupAverageTime = warmupSpentTime / warmupCount;
        return new WarmupInfo(warmupCount, warmupAverageTime);
    }

    private double[] measure(Runnable snippet, int warmupCount, double warmupTime) {
        if (measureFirstTimeOnly || (minMeasureCount == 1 && warmupTime >= allocatedMeasureSeconds * NANOS_PER_SECOND)) {
            resultPrinter.printInfoValue("warmupCount", 0);
            resultPrinter.printInfoValue("warmupTime", 0);
            resultPrinter.printInfoValue("runCountTime", 1);
            resultPrinter.printInfoValue("measurementCount", warmupCount);
            return new double[] { warmupTime };
        }

        int measurementCount;
        if (warmupTime == 0) {
            measurementCount = maxWarmupCount;
        } else {
            measurementCount = (int) (allocatedMeasureSeconds * NANOS_PER_SECOND / warmupTime);
        }
        measurementCount = Math.max(minMeasureCount, measurementCount);
        measurementCount = Math.min(maxMeasureCount, measurementCount);

        if (measurementCount >= runCount) {
            double[] measurements = new double[runCount];
            int singleMeasurementCount = measurementCount / runCount;
            resultPrinter.printInfoValue("warmupCount", warmupCount);
            resultPrinter.printInfoValue("warmupTime", warmupTime);
            resultPrinter.printInfoValue("runCountTime", runCount);
            resultPrinter.printInfoValue("measurementCount", singleMeasurementCount);
            for (int i = 0; i < runCount; i++) {
                measurements[i] = measure(snippet, singleMeasurementCount);
            }
            return measurements;
        } else {
            resultPrinter.printInfoValue("warmupCount", warmupCount);
            resultPrinter.printInfoValue("warmupTime", warmupTime);
            resultPrinter.printInfoValue("runCountTime", 1);
            resultPrinter.printInfoValue("measurementCount", measurementCount);
            return new double[] { measure(snippet, measurementCount) };
        }
    }

    private double measureWithTimeout(Runnable snippet, int repeat) {
        AtomicReference<Double> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            result.set(measure(snippet, repeat));
            latch.countDown();
        });
        thread.start();

        try {
            if (latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                return result.get();
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new RuntimeException("Interrupt");
        }

        thread.interrupt();

        return Double.POSITIVE_INFINITY;
    }

    private double measure(Runnable snippet, int repeat) {
        long startNanos = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            try {
                snippet.run();
            } catch (Exception ex) {
                // ignore
            }
        }
        long endNanos = System.nanoTime();
        double nanos = endNanos - startNanos;
        return nanos / repeat;
    }

    private static class WarmupInfo {
        public final int warmupCount;
        public final double warmupTime;

        public WarmupInfo(int warmupCount, double warmupTime) {
            this.warmupCount = warmupCount;
            this.warmupTime = warmupTime;
        }
    }
}
