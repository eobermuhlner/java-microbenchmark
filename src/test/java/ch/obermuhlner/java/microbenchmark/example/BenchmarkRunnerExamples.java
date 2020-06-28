package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkRunner;

import java.math.BigDecimal;

public class BenchmarkRunnerExamples {
    public static void main(String[] args) {
        BigDecimal value1 = BigDecimal.valueOf(1.23456);
        BigDecimal value2 = BigDecimal.valueOf(9.87654);

//        new BenchmarkRunner<Integer>()
//                .csvReport("const.csv")
//                .allocatedMeasureSeconds(2)
//                .forLoop(0, 10, i -> i)
//                .benchmark("const", i -> {
//                    value2.divide(value1, MathContext.DECIMAL128);
//                })
//                .run();

        new BenchmarkRunner<Integer>()
                .csvReport("busy.csv")
                .allocatedMeasureSeconds(0.1)
                .forLoop(0, 500, 10, i -> i)
                .benchmark("busy", i -> {
                    busyWait(i);
                })
                .run();

//        new BenchmarkRunner<Integer>()
//                .csvReport("sleep.csv")
//                .forLoop(0, i -> i < 50, i -> i+1)
//                .benchmark("nothing", millis -> {})
//                .benchmark("sleep", millis -> {
//                    try {
//                        Thread.sleep(millis);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .run();

//        new BenchmarkRunner<Integer>()
//                .csvReport("sleep2.csv")
//                .forLoop(0, i -> i <= 10, i -> i+1)
//                .forLoop(0, i -> i <= 10, i -> i+1)
//                .benchmark("sleep", (millis1, millis2) -> {
//                    try {
//                        Thread.sleep(millis1 + millis2);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .run();
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
