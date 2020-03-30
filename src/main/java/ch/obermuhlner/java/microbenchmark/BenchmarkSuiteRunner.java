package ch.obermuhlner.java.microbenchmark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkSuiteRunner<T> {

    private final CompositeResultPrinter resultPrinter;

    private final BenchmarkRunner benchmarkRunner;

    private List<T> arguments1;
    private List<T> arguments2;
    private List<String> names = new ArrayList<>();
    private List<Consumer<T>> benchmarkSnippets1 = new ArrayList<>();
    private List<BiConsumer<T, T>> benchmarkSnippets2 = new ArrayList<>();

    public BenchmarkSuiteRunner() {
        this(new BenchmarkRunner());
    }

    public BenchmarkSuiteRunner(BenchmarkRunner benchmarkRunner) {
        this.benchmarkRunner = benchmarkRunner;
        resultPrinter = new CompositeResultPrinter(new StdoutResultPrinter());
    }

    public BenchmarkSuiteRunner<T> csvReport(String fileName) {
        try {
            resultPrinter.addResultPrinter(new CsvResultPrinter(new PrintWriter(new BufferedWriter(new FileWriter(fileName)))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public BenchmarkSuiteRunner<T> forLoop(int startValue, int exclEndValue, Function<Integer, T> converter) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, converter);
    }

    public BenchmarkSuiteRunner<T> forLoop(int startValue, int exclEndValue, int step, Function<Integer, T> converter) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, converter);
    }

    public BenchmarkSuiteRunner<T> forLoop(T startValue, Predicate<T> condition, Function<T, T> stepFunction) {
        return forLoop(startValue, condition, stepFunction, t -> t);
    }

    public <A> BenchmarkSuiteRunner<T> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T> converter) {
        List<T> arguments = new ArrayList<>();

        A value = startValue;
        while (condition.test(value)) {
            arguments.add(converter.apply(value));
            value = stepFunction.apply(value);
        }

        return forArguments(arguments);
    }

    public BenchmarkSuiteRunner<T> forStream(Stream<T> stream) {
        return forArguments(stream.collect(Collectors.toList()));
    }

    public BenchmarkSuiteRunner<T> forArguments(T... arguments) {
        return forArguments(Arrays.asList(arguments));
    }

    public BenchmarkSuiteRunner<T> forArguments(List<T> arguments) {
        if (arguments1 == null) {
            arguments1 = arguments;
            return this;
        }
        if (arguments2 == null) {
            arguments2 = arguments;
            return this;
        }
        throw new RuntimeException("Too many argument dimensions");
    }

    public BenchmarkSuiteRunner<T> benchmark(String name, Consumer<T> snippet) {
        names.add(name);
        benchmarkSnippets1.add(snippet);
        return this;
    }

    public BenchmarkSuiteRunner<T> benchmark(String name, BiConsumer<T, T> snippet) {
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
        resultPrinter.printArguments(arguments1.stream().map(String::valueOf).collect(Collectors.toList()));

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Consumer<T> snippet = benchmarkSnippets1.get(i);
            for (T argument : arguments1) {
                double result = benchmarkRunner.measure(snippet, argument);
                resultPrinter.printBenchmark(name, String.valueOf(argument), result);
            }
        }

        resultPrinter.printFinished();
    }

    private void runSnippets2() {
        if (names.size() != 1) {
            throw new RuntimeException("Can only run exactly 1 two-dimensional benchmark");
        }

        resultPrinter.printDimensions(2);
        resultPrinter.printNames(arguments1.stream().map(String::valueOf).collect(Collectors.toList()));
        resultPrinter.printArguments(arguments2.stream().map(String::valueOf).collect(Collectors.toList()));

        BiConsumer<T, T> snippet = benchmarkSnippets2.get(0);

        for (T argument1 : arguments1) {
            for (T argument2 : arguments2) {
                double result = benchmarkRunner.measure(snippet, argument1, argument2);
                resultPrinter.printBenchmark(String.valueOf(argument1), String.valueOf(argument2), result);
            }
        }

        resultPrinter.printFinished();
    }

    public static void main(String[] args) {
        new BenchmarkSuiteRunner<Integer>()
                .csvReport("sleep.csv")
                .forLoop(0, i -> i < 100, i -> i+10)
                .benchmark("nothing", millis -> {})
                .benchmark("sleep", millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                })
                .run();

        new BenchmarkSuiteRunner<Integer>()
                .csvReport("sleep2.csv")
                .forLoop(0, i -> i <= 100, i -> i+25)
                .forLoop(0, i -> i <= 100, i -> i+20)
                .benchmark("sleep", (millis1, millis2) -> {
                    try {
                        Thread.sleep(millis1 + millis2);
                    } catch (InterruptedException e) {
                    }
                })
                .run();
    }
}
