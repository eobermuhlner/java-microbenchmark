package ch.obermuhlner.java.microbenchmark.runner.internal;

public class SimpleBenchmarkRunner extends AbstractBenchmarkRunner {
    public SimpleBenchmarkRunner(BenchmarkConfig config) {
        super(config);
    }

    public double measure(Runnable snippet) {
        AbstractBenchmarkRunner.WarmupInfo warmupInfo = warmup(snippet);
        double[] results = measure(snippet, warmupInfo.warmupCount, warmupInfo.warmupTime);
        double result = config.resultStrategy.apply(results);
        return result;
    }
}
