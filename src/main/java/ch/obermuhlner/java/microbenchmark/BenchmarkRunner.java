package ch.obermuhlner.java.microbenchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BenchmarkRunner {
    private static final double NANOS_PER_SECOND = 1_000_000_000;

    private double allocatedSeconds = 1.0;
    private double allocatedWarmupRatio = 0.1;
    private int maxWarmupCount = 10000;
    private long timeoutSeconds = 10;

    public <T> double measure(Consumer<T> snippet, T argument) {
        double firstTime = measureWithTimeout(snippet, argument, 1);
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

    private <T> double measureWithTimeout(Consumer<T> snippet, T argument, int repeat) {
        AtomicReference<Double> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            result.set(measure(snippet, argument, repeat));
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

    public <T> double measure(Consumer<T> snippet, T argument, int repeat) {
        long startNanos = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            try {
                snippet.accept(argument);
            } catch (Exception ex) {
                // ignore
            }
        }
        long endNanos = System.nanoTime();
        double seconds = (endNanos - startNanos) / NANOS_PER_SECOND;
        return seconds / repeat;
    }
}
