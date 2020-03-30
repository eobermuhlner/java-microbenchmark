package ch.obermuhlner.java.microbenchmark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkSuiteRunner<T> {

    private final CompositeResultPrinter resultPrinter;

    private final BenchmarkRunner benchmarkRunner;

    private Collection<T> arguments;
    private List<String> suiteNames = new ArrayList<>();
    private List<Consumer<T>> suiteSnippets = new ArrayList<>();

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

        forArguments(arguments);

        return this;
    }

    public BenchmarkSuiteRunner<T> forStream(Stream<T> stream) {
        return forArguments(stream.collect(Collectors.toList()));
    }

    public BenchmarkSuiteRunner<T> forArguments(T... arguments) {
        this.arguments = Arrays.asList(arguments);
        return this;
    }

    public BenchmarkSuiteRunner<T> forArguments(Collection<T> arguments) {
        this.arguments = arguments;
        return this;
    }

    public BenchmarkSuiteRunner<T> benchmark(String name, Consumer<T> snippet) {
        suiteNames.add(name);
        suiteSnippets.add(snippet);
        return this;
    }

    public void run() {
        try {
            resultPrinter.printNames(suiteNames);
            resultPrinter.printArguments(arguments.stream().map(String::valueOf).collect(Collectors.toList()));

            for (int i = 0; i < suiteNames.size(); i++) {
                String name = suiteNames.get(i);
                Consumer<T> snippet = suiteSnippets.get(i);
                for (T argument : arguments) {
                    double result = benchmarkRunner.measure(snippet, argument);
                    resultPrinter.printSuite(name, String.valueOf(argument), result);
                }
            }

            resultPrinter.printFinished();
        } finally {
            resultPrinter.close();
        }
    }

    public static void main(String[] args) {
        BenchmarkSuiteRunner<Integer> benchmarkSuiteRunner = new BenchmarkSuiteRunner<>();

        benchmarkSuiteRunner
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
    }
}
