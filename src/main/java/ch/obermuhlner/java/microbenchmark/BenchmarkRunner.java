package ch.obermuhlner.java.microbenchmark;

import java.util.function.Consumer;

public class BenchmarkRunner {
    private static final double NANOS_PER_SECOND = 1_000_000_000;

    private double allocatedSeconds = 1.0;
    private double allocatedWarmupRatio = 0.1;
    private int maxWarmupCount = 10000;

    public <T> double measure(Consumer<T> snippet, T argument) {
        double firstTime = measure(snippet, argument, 1);
        if (firstTime >= allocatedSeconds) {
            return firstTime;
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < allocatedSeconds * allocatedWarmupRatio && warmupCount < maxWarmupCount) {
            double warmupTime = measure(snippet, argument, 1);
            warmupSpentTime += warmupTime;
            warmupCount++;
        }

        double averageWarmupTime = warmupSpentTime / warmupCount;
        int measureRepeat;
        if (warmupSpentTime == 0) {
            measureRepeat = maxWarmupCount;
        } else {
            double allocatedMeasureSeconds = allocatedSeconds * (1.0 - allocatedWarmupRatio);
            measureRepeat = (int) (allocatedMeasureSeconds / averageWarmupTime);
        }
        if (measureRepeat <= 0) {
            measureRepeat = 1;
        }

        return measure(snippet, argument, measureRepeat);
    }

    public <T> double measure(Consumer<T> snippet, T argument, int repeat) {
        long startNanos = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            snippet.accept(argument);
        }
        long endNanos = System.nanoTime();
        double seconds = (endNanos - startNanos) / NANOS_PER_SECOND;
        return seconds / repeat;
    }
}
