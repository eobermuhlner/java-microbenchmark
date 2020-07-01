package ch.obermuhlner.java.microbenchmark.runner.internal;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractBenchmarkRunner {
    protected final BenchmarkConfig config;

    public AbstractBenchmarkRunner(BenchmarkConfig config) {
        this.config = config;
    }

    protected WarmupInfo warmup(Runnable snippet) {
        double firstTime = measureWithTimeout(snippet, 1);
        if (config.measureFirstTimeOnly || firstTime >= config.allocatedMeasureSeconds * BenchmarkRunner.NANOS_PER_SECOND) {
            return new WarmupInfo(1, firstTime);
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < config.allocatedWarmupSeconds * BenchmarkRunner.NANOS_PER_SECOND && warmupCount >= config.minWarmupCount && warmupCount < config.maxWarmupCount) {
            double warmupTime = measure(snippet, 1);
            warmupSpentTime += warmupTime;
            warmupCount++;
        }

        double warmupAverageTime = warmupSpentTime / warmupCount;
        return new WarmupInfo(warmupCount, warmupAverageTime);
    }

    protected double[] measure(Runnable snippet, int warmupCount, double warmupTime) {
        if (config.measureFirstTimeOnly || (config.minMeasureCount == 1 && warmupTime >= config.allocatedMeasureSeconds * BenchmarkRunner.NANOS_PER_SECOND)) {
            config.resultPrinter.printInfoValue("warmupCount", 0);
            config.resultPrinter.printInfoValue("warmupTime", 0);
            config.resultPrinter.printInfoValue("runCountTime", 1);
            config.resultPrinter.printInfoValue("measurementCount", warmupCount);
            return new double[] { warmupTime };
        }

        int measurementCount;
        if (warmupTime == 0) {
            measurementCount = config.maxWarmupCount;
        } else {
            measurementCount = (int) (config.allocatedMeasureSeconds * BenchmarkRunner.NANOS_PER_SECOND / warmupTime);
        }
        measurementCount = Math.max(config.minMeasureCount, measurementCount);
        measurementCount = Math.min(config.maxMeasureCount, measurementCount);

        if (measurementCount >= config.runCount) {
            double[] measurements = new double[config.runCount];
            int singleMeasurementCount = measurementCount / config.runCount;
            config.resultPrinter.printInfoValue("warmupCount", warmupCount);
            config.resultPrinter.printInfoValue("warmupTime", warmupTime);
            config.resultPrinter.printInfoValue("runCountTime", config.runCount);
            config.resultPrinter.printInfoValue("measurementCount", singleMeasurementCount);
            for (int i = 0; i < config.runCount; i++) {
                measurements[i] = measure(snippet, singleMeasurementCount);
            }
            return measurements;
        } else {
            config.resultPrinter.printInfoValue("warmupCount", warmupCount);
            config.resultPrinter.printInfoValue("warmupTime", warmupTime);
            config.resultPrinter.printInfoValue("runCountTime", 1);
            config.resultPrinter.printInfoValue("measurementCount", measurementCount);
            return new double[] { measure(snippet, measurementCount) };
        }
    }

    protected double measureWithTimeout(Runnable snippet, int repeat) {
        AtomicReference<Double> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            result.set(measure(snippet, repeat));
            latch.countDown();
        });
        thread.start();

        try {
            if (latch.await(config.timeoutSeconds, TimeUnit.SECONDS)) {
                return result.get();
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new RuntimeException("Interrupt");
        }

        thread.interrupt();

        return Double.POSITIVE_INFINITY;
    }

    protected double measure(Runnable snippet, int repeat) {
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

    static class WarmupInfo {
        public final int warmupCount;
        public final double warmupTime;

        public WarmupInfo(int warmupCount, double warmupTime) {
            this.warmupCount = warmupCount;
            this.warmupTime = warmupTime;
        }
    }
}
