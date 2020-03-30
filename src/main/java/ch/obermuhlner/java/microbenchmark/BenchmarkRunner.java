package ch.obermuhlner.java.microbenchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BenchmarkRunner {
    private static final double NANOS_PER_SECOND = 1_000_000_000;

    private double allocatedSeconds;
    private double allocatedWarmupRatio;
    private int maxWarmupCount;
    private long timeoutSeconds;

    public BenchmarkRunner() {
        this(1.0, 0.1, 10000, 10);
    }

    public BenchmarkRunner(double allocatedSeconds, double allocatedWarmupRatio, int maxWarmupCount, long timeoutSeconds) {
        this.allocatedSeconds = allocatedSeconds;
        this.allocatedWarmupRatio = allocatedWarmupRatio;
        this.maxWarmupCount = maxWarmupCount;
        this.timeoutSeconds = timeoutSeconds;
    }

    public <T> double measure(Consumer<T> snippet, T argument) {
        return measure(() -> snippet.accept(argument));
    }

    public <T> double measure(BiConsumer<T, T> snippet, T argument1, T argument2) {
        return measure(() -> snippet.accept(argument1, argument2));
    }

    public double measure(Runnable snippet) {
        double firstTime = measureWithTimeout(snippet, 1);
        if (firstTime >= allocatedSeconds) {
            return firstTime;
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < allocatedSeconds * allocatedWarmupRatio && warmupCount < maxWarmupCount) {
            double warmupTime = measure(snippet, 1);
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

        return measure(snippet, measureRepeat);
    }

    private <T> double measureWithTimeout(Runnable snippet, int repeat) {
        AtomicReference<Double> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            result.set(measure(snippet, repeat));
            latch.countDown();
        }).start();

        try {
            if (latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                return result.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupt");
        }

        return Double.POSITIVE_INFINITY;
    }

    public <T> double measure(Runnable snippet, int repeat) {
        long startNanos = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            try {
                snippet.run();
            } catch (Exception ex) {
                // ignore
            }
        }
        long endNanos = System.nanoTime();
        double seconds = (endNanos - startNanos) / NANOS_PER_SECOND;
        return seconds / repeat;
    }
}
