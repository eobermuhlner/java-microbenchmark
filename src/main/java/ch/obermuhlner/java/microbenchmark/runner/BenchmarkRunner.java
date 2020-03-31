package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.printer.CompositeResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.CsvResultPrinter;
import ch.obermuhlner.java.microbenchmark.printer.SimpleResultPrinter;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkRunner<T> {

    private static final double NANOS_PER_SECOND = 1_000_000_000;

    private final CompositeResultPrinter resultPrinter;

    private double allocatedMeasureSeconds = 1.0;
    private double allocatedWarmupSeconds = 0.1;
    private int maxWarmupCount = 100_000;
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

    public BenchmarkRunner<T> maxWarmupCount(int maxWarmupCount) {
        this.maxWarmupCount = maxWarmupCount;
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

        double[] warmupTimes = new double[names.size() * arguments1.size()];

        for (int i = 0; i < names.size(); i++) {
            Consumer<T> snippet = benchmarkSnippets1.get(i);
            for (int j = 0; j < arguments1.size(); j++) {
                T argument = arguments1.get(j);
                warmupTimes[i+j*names.size()] = warmup(snippet, argument);
            }
        }

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Consumer<T> snippet = benchmarkSnippets1.get(i);
            for (int j = 0; j < arguments1.size(); j++) {
                T argument = arguments1.get(j);
                String argumentName = arguments1Names.get(j);
                double warmupTime = warmupTimes[i+j*names.size()];
                double result = measure(snippet, argument, warmupTime);
                resultPrinter.printBenchmark(name, argumentName, result);
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

        double[] warmupTimes = new double[names.size() * arguments1.size()];

        for (int i = 0; i < arguments1.size(); i++) {
            T argument1 = arguments1.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T argument2 = arguments2.get(j);
                warmupTimes[i+j*names.size()] = warmup(snippet, argument1, argument2);
            }
        }

        for (int i = 0; i < arguments1.size(); i++) {
            T argument1 = arguments1.get(i);
            String argument1Name = arguments1Names.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T argument2 = arguments2.get(j);
                String argument2Name = arguments2Names.get(j);

                double warmupTime = warmupTimes[i+j*names.size()];
                double result = measure(snippet, argument1, argument2, warmupTime);
                resultPrinter.printBenchmark(argument1Name, argument2Name, result);
            }
        }

        resultPrinter.printFinished();
    }

    public double warmup(Consumer<T> snippet, T argument) {
        return warmup(() -> snippet.accept(argument));
    }

    public double measure(Consumer<T> snippet, T argument, double warmupTime) {
        return measure(() -> snippet.accept(argument), warmupTime);
    }

    public double warmup(BiConsumer<T, T> snippet, T argument1, T argument2) {
        return warmup(() -> snippet.accept(argument1, argument2));
    }

    public double measure(BiConsumer<T, T> snippet, T argument1, T argument2, double warmupTime) {
        return measure(() -> snippet.accept(argument1, argument2), warmupTime);
    }

    public double warmup(Runnable snippet) {
        double firstTime = measureWithTimeout(snippet, 1);
        if (measureFirstTimeOnly || firstTime >= allocatedMeasureSeconds * NANOS_PER_SECOND) {
            return firstTime;
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < allocatedWarmupSeconds * NANOS_PER_SECOND && warmupCount < maxWarmupCount) {
            double warmupTime = measure(snippet, 1);
            warmupSpentTime += warmupTime;
            warmupCount++;
        }

        double warmupAverageTime = warmupSpentTime / warmupCount;

        return warmupAverageTime;
    }

    public double measure(Runnable snippet, double warmupTime) {
        if (measureFirstTimeOnly || warmupTime >= allocatedMeasureSeconds * NANOS_PER_SECOND) {
            return warmupTime;
        }

        int measurementCount;
        if (warmupTime == 0) {
            measurementCount = maxWarmupCount;
        } else {
            measurementCount = (int) (allocatedMeasureSeconds * NANOS_PER_SECOND / warmupTime);
        }
        measurementCount = Math.max(1, measurementCount);

        if (measurementCount >= runCount) {
            int halfRunCount = Math.max(1, runCount / 2);
            double[] measurements = new double[runCount];
            int singleMeasurementCount = measurementCount / runCount;
            for (int i = 0; i < runCount; i++) {
                measurements[i] = measure(snippet, singleMeasurementCount);
            }
            Arrays.sort(measurements);
            double totalMeasurement = 0;
            for (int i = 0; i < halfRunCount; i++) {
                totalMeasurement += measurements[i];
            }
            return totalMeasurement / halfRunCount;
        } else {
            return measure(snippet, measurementCount);
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

    public double measure(Runnable snippet, int repeat) {
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

    public static void main(String[] args) {
        BigDecimal value1 = BigDecimal.valueOf(1.23456);
        BigDecimal value2 = BigDecimal.valueOf(9.87654);

        new BenchmarkRunner<Integer>()
                .csvReport("const.csv")
                .allocatedMeasureSeconds(2)
                .forLoop(0, 10, i -> i)
                .benchmark("const", i -> {
                    value2.divide(value1, MathContext.DECIMAL128);
                })
                .run();

//        new BenchmarkRunner<Integer>()
//                .csvReport("sleep.csv")
//                .forLoop(0, i -> i < 50, i -> i+1)
//                .benchmark("nothing", millis -> {})
//                .benchmark("sleep", millis -> {
//                    try {
//                        Thread.sleep(millis);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .run();
//
//        new BenchmarkRunner<Integer>()
//                .csvReport("sleep2.csv")
//                .forLoop(0, i -> i <= 10, i -> i+1)
//                .forLoop(0, i -> i <= 10, i -> i+1)
//                .benchmark("sleep", (millis1, millis2) -> {
//                    try {
//                        Thread.sleep(millis1 + millis2);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .run();
    }
}
