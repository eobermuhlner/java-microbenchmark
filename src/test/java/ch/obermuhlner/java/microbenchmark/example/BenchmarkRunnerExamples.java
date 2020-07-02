package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkRunner;
import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.math.BigDecimal;
import java.math.MathContext;

public class BenchmarkRunnerExamples {
    public static void main(String[] args) {
//        new BenchmarkRunner()
//                .csvReport("const.csv")
//                .allocatedMeasureSeconds(0.1)
//                .forLoop(0, 10)
//                .benchmark("const", i -> {
//                    BigDecimal.valueOf(9.87654).divide(BigDecimal.valueOf(1.23456), MathContext.DECIMAL128);
//                })
//                .run();
//
//        new BenchmarkRunner()
//                .verbose(true)
//                .csvReport("busy.csv")
//                .allocatedMeasureSeconds(0.1)
//                .forLoop(0, 500, 100)
//                .benchmark("busy", i -> {
//                    busyWait(i);
//                })
//                .run();

//        new BenchmarkRunner()
//                .verbose(true)
//                .csvReport("nothing.csv")
//                .forLoop(0, 50)
//                .benchmark("nothing", x -> {})
//                .run();

//        new BenchmarkRunner()
//                .csvReport("sleep.csv")
//                .allocatedMeasureSeconds(0.1)
//                //.timeUnit(TimeUnit.MicroSeconds)
//                .forLoop(0, 50)
//                .benchmark("sleep", millis -> {
//                    try {
//                        Thread.sleep(millis);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .benchmark("busy", millis -> {
//                    busyWait(millis * 1_000_000);
//                })
//                .run();

//        new BenchmarkRunner()
//                .verbose(true)
//                .csvReport("sleep2.csv")
//                .allocatedWarmupSeconds(0)
//                .allocatedMeasureSeconds(0.1)
//                .timeUnit(TimeUnit.MilliSeconds)
//                .forLoop(0, 20)
//                .forLoop(0, 20)
//                .benchmark("sleep", (millis1, millis2) -> {
//                    try {
//                        Thread.sleep(millis1 + millis2);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .run();

        double elapsed = new BenchmarkRunner()
                .timeUnit(TimeUnit.MilliSeconds)
                .measure(millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                }, 1234);
        System.out.println("sleep(1234) = " + elapsed + " millis");
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
