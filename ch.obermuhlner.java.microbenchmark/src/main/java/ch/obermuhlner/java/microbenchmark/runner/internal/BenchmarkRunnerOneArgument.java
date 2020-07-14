package ch.obermuhlner.java.microbenchmark.runner.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkRunnerOneArgument<T1> extends AbstractBenchmarkRunner {
    private List<T1> arguments1;
    private List<String> arguments1Names;
    private List<Consumer<T1>> benchmarkSnippets1 = new ArrayList<>();

    public BenchmarkRunnerOneArgument(BenchmarkConfig config, List<T1> arguments, List<String> argumentsNames) {
        super(config);
        arguments1 = arguments;
        arguments1Names = argumentsNames;
    }

    public BenchmarkRunnerTwoArguments<T1, Integer> forLoop(int startValue, int exclEndValue) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, Function.identity());
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forLoop(int startValue, int exclEndValue, Function<Integer, T2> converter) {
        return forLoop(startValue, exclEndValue, startValue<exclEndValue ? 1 : -1, converter);
    }

    public BenchmarkRunnerTwoArguments<T1, Integer> forLoop(int startValue, int exclEndValue, int step) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, Function.identity());
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forLoop(int startValue, int exclEndValue, int step, Function<Integer, T2> converter) {
        return forLoop(startValue, i -> i < exclEndValue, i -> i + step, converter);
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forLoop(T2 startValue, Predicate<T2> condition, Function<T2, T2> stepFunction) {
        return forLoop(startValue, condition, stepFunction, Function.identity());
    }

    public <A, T2> BenchmarkRunnerTwoArguments<T1, T2> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T2> converter) {
        return forLoop(startValue, condition, stepFunction, converter, String::valueOf);
    }

    public <A, T2> BenchmarkRunnerTwoArguments<T1, T2> forLoop(A startValue, Predicate<A> condition, Function<A, A> stepFunction, Function<A, T2> converter, Function<A, String> argumentToNameFunction) {
        List<T2> arguments = new ArrayList<>();
        List<String> argumentsNames = new ArrayList<>();

        A value = startValue;
        while (condition.test(value)) {
            arguments.add(converter.apply(value));
            argumentsNames.add(argumentToNameFunction.apply(value));
            value = stepFunction.apply(value);
        }

        return forArguments(arguments, argumentsNames);
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forArguments(Stream<T2> stream) {
        return forArguments(stream.collect(Collectors.toList()));
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forArguments(T2... arguments) {
        return forArguments(Arrays.asList(arguments));
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forArguments(List<T2> arguments) {
        return forArguments(arguments, String::valueOf);
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forArguments(List<T2> arguments, Function<T2, String> argumentToNameFunction) {
        List<String> argumentsNames = arguments.stream().map(argumentToNameFunction).collect(Collectors.toList());
        return forArguments(arguments, argumentsNames);
    }

    public <T2> BenchmarkRunnerTwoArguments<T1, T2> forArguments(List<T2> arguments, List<String> argumentsNames) {
        return new BenchmarkRunnerTwoArguments<T1, T2>(config, arguments1,  arguments1Names, arguments, argumentsNames);
    }

    public BenchmarkRunnerOneArgument<T1> benchmark(String name, Consumer<T1> snippet) {
        config.names.add(name);
        benchmarkSnippets1.add(snippet);
        return this;
    }

    public void run() {
        try {
            runSnippets1();
        } finally {
            config.resultPrinter.close();
        }
    }

    private void runSnippets1() {
        config.resultPrinter.setTimeUnit(config.getTimeUnit());

        config.resultPrinter.printDimensions(1);
        config.resultPrinter.printNames(config.names);
        config.resultPrinter.printArguments(arguments1Names);

        WarmupInfo[] warmupInfos = new WarmupInfo[config.names.size() * arguments1.size()];

        for (int i = 0; i < config.names.size(); i++) {
            Consumer<T1> snippet = benchmarkSnippets1.get(i);
            preWarmup(snippet, arguments1.get(0));
            for (int j = 0; j < arguments1.size(); j++) {
                T1 argument = arguments1.get(j);
                warmupInfos[i+j*config.names.size()] = warmup(snippet, argument);
            }
        }

        for (int i = 0; i < config.names.size(); i++) {
            String name = config.names.get(i);
            Consumer<T1> snippet = benchmarkSnippets1.get(i);
            for (int j = 0; j < arguments1.size(); j++) {
                T1 argument = arguments1.get(j);
                String argumentName = arguments1Names.get(j);
                WarmupInfo warmupInfo = warmupInfos[i+j*config.names.size()];
                double[] results = measure(snippet, argument, config.getPreWarmupCount(), warmupInfo.warmupCount, warmupInfo.warmupTime);
                double result = config.resultCalculator.apply(results);
                config.resultPrinter.printBenchmark(name, argumentName, result, results);
            }
        }

        config.resultPrinter.printFinished();
    }

    private WarmupInfo preWarmup(Consumer<T1> snippet, T1 argument) {
        return preWarmup(() -> snippet.accept(argument));
    }

    private WarmupInfo warmup(Consumer<T1> snippet, T1 argument) {
        return warmup(() -> snippet.accept(argument));
    }

    private double[] measure(Consumer<T1> snippet, T1 argument, int preWarmupCount, int warmupCount, double warmupTime) {
        return measure(() -> snippet.accept(argument), preWarmupCount, warmupCount, warmupTime);
    }
}
