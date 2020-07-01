package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkRunner;

public class BenchmarkRunnerExamples {
    public static void main(String[] args) {
//        new BenchmarkRunner()
//                .csvReport("const.csv")
//                .allocatedMeasureSeconds(1)
//                .forLoop(0, 10)
//                .benchmark("const", i -> {
//                    BigDecimal.valueOf(9.87654).divide(BigDecimal.valueOf(1.23456), MathContext.DECIMAL128);
//                })
//                .run();

//        new BenchmarkRunner()
//                .verbose(true)
//                .csvReport("busy.csv")
//                .allocatedMeasureSeconds(0.2)
//                .forLoop(0, 500, 100)
//                .benchmark("busy", i -> {
//                    busyWait(i);
//                })
//                .run();

        new BenchmarkRunner()
                .csvReport("sleep.csv")
                .allocatedMeasureSeconds(0.1)
                .forLoop(10, 50)
                .benchmark("nothing", millis -> {})
                .benchmark("sleep", millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                })
                .run();

        new BenchmarkRunner()
                .csvReport("sleep2.csv")
                .allocatedMeasureSeconds(0.1)
                .forLoop(0, 100, 10)
                .forLoop(0, 100, 10)
                .benchmark("sleep", (millis1, millis2) -> {
                    try {
                        Thread.sleep(millis1 + millis2);
                    } catch (InterruptedException e) {
                    }
                })
                .run();
    }

    private static void busyWait(long nanos) {
        long startNanos = System.nanoTime();
        long targetNanos = startNanos + nanos;
        long endNanos;
        do {
            endNanos = System.nanoTime();
        } while (endNanos < targetNanos);
    }
}
