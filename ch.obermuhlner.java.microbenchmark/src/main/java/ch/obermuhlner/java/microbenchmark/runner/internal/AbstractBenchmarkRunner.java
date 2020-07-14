package ch.obermuhlner.java.microbenchmark.runner.internal;

import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractBenchmarkRunner {
    protected final BenchmarkConfig config;

    public AbstractBenchmarkRunner(BenchmarkConfig config) {
        this.config = config;
    }

    protected WarmupInfo preWarmup(Runnable snippet) {
        if (config.getPreWarmupCount() > 0) {
            double preWarmupTime = measureNanoseconds(snippet, config.getPreWarmupCount());
            return new WarmupInfo(config.getPreWarmupCount(), preWarmupTime);
        }

        return new WarmupInfo(0, 0);
    }

    protected WarmupInfo warmup(Runnable snippet) {
        double firstTime = measureNanosecondsWithTimeout(snippet, 1);
        if (config.isMeasureFirstTimeOnly() || firstTime >= TimeUnit.Seconds.toNanoSeconds(config.getAllocatedMeasureSeconds())) {
            return new WarmupInfo(1, firstTime);
        }

        double warmupSpentTime = firstTime;
        int warmupCount = 1;
        while (warmupSpentTime < TimeUnit.Seconds.toNanoSeconds(config.getAllocatedWarmupSeconds()) && warmupCount >= config.getMinWarmupCount() && warmupCount < config.getMinWarmupCount()) {
            double warmupTime = measureNanoseconds(snippet, 1);
            warmupSpentTime += warmupTime;
            warmupCount++;
        }

        double warmupAverageTime = warmupSpentTime / warmupCount;
        return new WarmupInfo(warmupCount, warmupAverageTime);
    }

    protected double[] measure(Runnable snippet, int preWarmupCount, int warmupCount, double warmupTime) {
        if (preWarmupCount > 0) {
            config.resultPrinter.printInfoValue("preWarmupCount", preWarmupCount);
        }

        if (config.isMeasureFirstTimeOnly() || (config.getMinMeasureCount() == 1 && warmupTime >= TimeUnit.Seconds.toNanoSeconds(config.getAllocatedMeasureSeconds()))) {
            config.resultPrinter.printInfoValue("warmupCount", 0);
            config.resultPrinter.printInfoValue("warmupTime", 0);
            config.resultPrinter.printInfoValue("runCountTime", 1);
            config.resultPrinter.printInfoValue("measurementCount", warmupCount);
            sleep();
            return new double[] { convertToTimeUnit(warmupTime) };
        }

        int measurementCount;
        if (warmupTime == 0) {
            measurementCount = config.getMaxWarmupCount();
        } else {
            measurementCount = (int) (TimeUnit.Seconds.toNanoSeconds(config.getAllocatedMeasureSeconds()) / warmupTime);
        }
        measurementCount = Math.max(config.getMinMeasureCount(), measurementCount);
        measurementCount = Math.min(config.getMaxMeasureCount(), measurementCount);

        if (measurementCount >= config.runCount) {
            double[] measurements = new double[config.runCount];
            int singleMeasurementCount = measurementCount / config.runCount;
            config.resultPrinter.printInfoValue("warmupCount", warmupCount);
            config.resultPrinter.printInfoValue("warmupTime", warmupTime);
            config.resultPrinter.printInfoValue("runCountTime", config.runCount);
            config.resultPrinter.printInfoValue("measurementCount", singleMeasurementCount);
            for (int i = 0; i < config.runCount; i++) {
                measurements[i] = convertToTimeUnit(measureNanoseconds(snippet, singleMeasurementCount));
                sleep();
            }
            return measurements;
        } else {
            config.resultPrinter.printInfoValue("warmupCount", warmupCount);
            config.resultPrinter.printInfoValue("warmupTime", warmupTime);
            config.resultPrinter.printInfoValue("runCountTime", 1);
            config.resultPrinter.printInfoValue("measurementCount", measurementCount);
            double[] measurement = new double[] { convertToTimeUnit(measureNanoseconds(snippet, measurementCount)) };
            sleep();
            return measurement;
        }
    }

    private void sleep() {
        long millis = (long) (config.getAllocatedSleepSeconds() * 1000);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted");
        }
    }

    protected double measureNanosecondsWithTimeout(Runnable snippet, int repeat) {
        AtomicReference<Double> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            result.set(measureNanoseconds(snippet, repeat));
            latch.countDown();
        });
        thread.start();

        try {
            if (latch.await(config.getTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS)) {
                return result.get();
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new RuntimeException("Interrupt");
        }

        thread.interrupt();

        return Double.POSITIVE_INFINITY;
    }

    protected double measureNanoseconds(Runnable snippet, int repeat) {
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

    public double convertToTimeUnit(double nanoseconds) {
        return config.getTimeUnit().nanosecondsToTimeUnit(nanoseconds);
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
