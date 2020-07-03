package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkBuilder;
import ch.obermuhlner.java.microbenchmark.runner.ResultCalculators;
import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.*;

public class BenchmarkRunnerExamples {
    public static void main(String[] args) {
        exampleBenchmarks();

        //experimentalBenchmarks();
    }

    public static void exampleBenchmarks() {
//        exampleSimpleMeasure1();
//        exampleSimpleMeasure2();
        exampleBenchmarkSleep1();
        exampleBenchmarkSleep2();
        exampleBenchmarkBigDecimalDivide();
    }

    public static void exampleSimpleMeasure1() {
        double elapsedMillis = new BenchmarkBuilder()
                .timeUnit(TimeUnit.MilliSeconds)
                .measure(millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                }, 1234);
        System.out.println("sleep(1234) = " + elapsedMillis + " millis");
    }

    public static void exampleSimpleMeasure2() {
        double elapsedMillis = new BenchmarkBuilder()
                .timeUnit(TimeUnit.MilliSeconds)
                .allocatedWarmupSeconds(0.5)
                .allocatedMeasureSeconds(2.0)
                .resultCalculator(ResultCalculators.MEDIAN)
                .measure(millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                }, 1234);
        System.out.println("sleep(1234) = " + elapsedMillis + " millis");
    }

    public static void exampleBenchmarkSleep1() {
        new BenchmarkBuilder()
                .csvReport("example_sleep_0_to_100.csv")
                .allocatedMeasureSeconds(0.1)
                .timeUnit(TimeUnit.MicroSeconds)
                .forLoop(0, 100)
                .benchmark("sleep", millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                })
                .benchmark("busy", millis -> {
                    busyWait(millis * 1_000_000);
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

    public static void exampleBenchmarkSleep2() {
        new BenchmarkBuilder()
                .csvReport("example_sleep_1_10_100_1000.csv")
                .allocatedMeasureSeconds(0.1)
                .timeUnit(TimeUnit.MicroSeconds)
                .forArguments(1, 10, 100, 1000)
                .benchmark("sleep", millis -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                    }
                })
                .benchmark("busy", millis -> {
                    busyWait(millis * 1_000_000);
                })
                .run();
    }

    public static void exampleBenchmarkBigDecimalDivide() {
        BigDecimal v1 = valueOf(1);
        BigDecimal v7 = valueOf(7);
        new BenchmarkBuilder()
                .csvReport("example_BigDecimal_divide_precision_1_to_1000.csv")
                .forLoop(1, 1000, i -> new MathContext(i))
                .benchmark("divide", mc -> {
                    v1.divide(v7, mc);
                })
                .run();
    }

    public static void experimentalBenchmarks() {
//        new BenchmarkBuilder()
//                .csvReport("const.csv")
//                .allocatedMeasureSeconds(0.1)
//                .forLoop(0, 10)
//                .benchmark("const", i -> {
//                    BigDecimal.valueOf(9.87654).divide(BigDecimal.valueOf(1.23456), MathContext.DECIMAL128);
//                })
//                .run();
//
//        new BenchmarkBuilder()
//                .verbose(true)
//                .csvReport("busy.csv")
//                .allocatedMeasureSeconds(0.1)
//                .forLoop(0, 500, 100)
//                .benchmark("busy", i -> {
//                    busyWait(i);
//                })
//                .run();

//        new BenchmarkBuilder()
//                .verbose(true)
//                .csvReport("nothing.csv")
//                .forLoop(0, 50)
//                .benchmark("nothing", x -> {})
//                .run();

//        new BenchmarkBuilder()
//                .csvReport("sleep.csv")
//                .timeUnit(TimeUnit.MicroSeconds)
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

//        new BenchmarkBuilder()
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

//        BigDecimal value1 = BigDecimal.valueOf(1.23456);
//        new BenchmarkBuilder()
//                .csvReport("BigDecimal.csv")
//                .allocatedMeasureSeconds(0.1)
//                .forLoop(
//                        BigDecimal.valueOf(0),
//                        b -> b.compareTo(BigDecimal.valueOf(10)) < 0,
//                        b -> b.add(BigDecimal.valueOf(0.01)))
//                .benchmark("divide", x -> {
//                    value1.divide(x, MathContext.DECIMAL128);
//                })
//                .run();

        BigDecimal v1 = valueOf(1);
        BigDecimal v7 = valueOf(7);
        MathContext mc1000 = new MathContext(1000);
        new BenchmarkBuilder()
                .csvReport("calibrate_warmup.csv")
                .warmupCount(0)
                .measureCount(100)
                .resultCalculator(ResultCalculators.AVERAGE)
                .forLoop(0, 10000)
                .benchmark("divide", i -> {
                    v1.divide(v7, mc1000);
                })
                .run();
    }
}
