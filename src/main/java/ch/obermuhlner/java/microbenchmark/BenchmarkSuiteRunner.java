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
import java.util.stream.Collectors;

public class BenchmarkSuiteRunner<T> {

    private final ResultPrinter resultPrinter;

    private final BenchmarkRunner benchmarkRunner;

    private Collection<T> arguments;
    private List<String> suiteNames = new ArrayList<>();
    private List<Consumer<T>> suiteSnippets = new ArrayList<>();

    public BenchmarkSuiteRunner() {
        this(new StdoutResultPrinter());
    }

    public BenchmarkSuiteRunner(ResultPrinter resultPrinter) {
        this(resultPrinter, new BenchmarkRunner());
    }

    public BenchmarkSuiteRunner(ResultPrinter resultPrinter, BenchmarkRunner benchmarkRunner) {
        this.resultPrinter = resultPrinter;
        this.benchmarkRunner = benchmarkRunner;
    }

    public BenchmarkSuiteRunner arguments(T... arguments) {
        this.arguments = Arrays.asList(arguments);
        return this;
    }

    public BenchmarkSuiteRunner arguments(Collection<T> arguments) {
        this.arguments = arguments;
        return this;
    }

    public BenchmarkSuiteRunner suite(String name, Consumer<T> snippet) {
        suiteNames.add(name);
        suiteSnippets.add(snippet);
        return this;
    }

    public void run() {
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
    }

    public static void main(String[] args) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("sleep.csv")))) {
            ResultPrinter printer = new CompositeResultPrinter(
                    new StdoutResultPrinter(),
                    new CsvResultPrinter(out));
            IntegerBenchmarkSuiteRunner benchmarkSuiteRunner = new IntegerBenchmarkSuiteRunner(printer);

            benchmarkSuiteRunner
                    .forLoop(0, 1000, 100)
                    .suite("sleep", millis -> {
                        try {
                            Thread.sleep(millis);
                        } catch (InterruptedException e) {
                        }
                    })
                    .run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
