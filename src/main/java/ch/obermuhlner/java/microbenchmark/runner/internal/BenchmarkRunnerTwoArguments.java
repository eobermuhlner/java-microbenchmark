package ch.obermuhlner.java.microbenchmark.runner.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class BenchmarkRunnerTwoArguments<T1, T2> extends AbstractBenchmarkRunner {

    private List<T1> arguments1;
    private List<T2> arguments2;
    private List<String> arguments1Names;
    private List<String> arguments2Names;

    private List<BiConsumer<T1, T2>> benchmarkSnippets2 = new ArrayList<>();

    public BenchmarkRunnerTwoArguments(BenchmarkConfig config, List<T1> arguments1, List<String> arguments1Names, List<T2> arguments2, List<String> arguments2Names) {
        super(config);

        this.arguments1 = arguments1;
        this.arguments1Names = arguments1Names;
        this.arguments2 = arguments2;
        this.arguments2Names = arguments2Names;
    }

    public BenchmarkRunnerTwoArguments<T1, T2> benchmark(String name, BiConsumer<T1, T2> snippet) {
        config.names.add(name);
        benchmarkSnippets2.add(snippet);
        return this;
    }

    public void run() {
        try {
            runSnippets2();
        } finally {
            config.resultPrinter.close();
        }
    }

    private void runSnippets2() {
        if (config.names.size() != 1) {
            throw new RuntimeException("Can only run exactly 1 two-dimensional benchmark");
        }

        config.resultPrinter.setTimeUnit(config.timeUnit);

        config.resultPrinter.printDimensions(2);
        config.resultPrinter.printNames(arguments1Names);
        config.resultPrinter.printArguments(arguments2Names);

        BiConsumer<T1, T2> snippet = benchmarkSnippets2.get(0);

        WarmupInfo[] warmupInfos = new WarmupInfo[arguments1.size() * arguments2.size()];

        for (int i = 0; i < arguments1.size(); i++) {
            T1 argument1 = arguments1.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T2 argument2 = arguments2.get(j);
                warmupInfos[i+j*arguments1.size()] = warmup(snippet, argument1, argument2);
            }
        }

        for (int i = 0; i < arguments1.size(); i++) {
            T1 argument1 = arguments1.get(i);
            String argument1Name = arguments1Names.get(i);
            for (int j = 0; j < arguments2.size(); j++) {
                T2 argument2 = arguments2.get(j);
                String argument2Name = arguments2Names.get(j);

                WarmupInfo warmupInfo = warmupInfos[i+j*arguments1.size()];
                double[] results = measure(snippet, argument1, argument2, warmupInfo.warmupCount, warmupInfo.warmupTime);
                double result = config.resultStrategy.apply(results);
                config.resultPrinter.printBenchmark(argument1Name, argument2Name, result, results);
            }
        }

        config.resultPrinter.printFinished();
    }

    private WarmupInfo warmup(BiConsumer<T1, T2> snippet, T1 argument1, T2 argument2) {
        return warmup(() -> snippet.accept(argument1, argument2));
    }

    private double[] measure(BiConsumer<T1, T2> snippet, T1 argument1, T2 argument2, int warmupCount, double warmupTime) {
        return measureNanoseconds(() -> snippet.accept(argument1, argument2), warmupCount, warmupTime);
    }
}
