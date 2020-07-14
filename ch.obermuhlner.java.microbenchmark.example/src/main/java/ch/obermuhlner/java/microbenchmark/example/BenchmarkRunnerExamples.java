package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.runner.BenchmarkBuilder;
import ch.obermuhlner.java.microbenchmark.runner.ResultCalculators;
import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;
import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.*;

public class BenchmarkRunnerExamples {
    public static void main(String[] args) {
        //exampleBenchmarks();
        //experimentalBenchmarks();
        calibrateBenchmarks();
    }

    public static void exampleBenchmarks() {
//        exampleSimpleMeasure1();
//        exampleSimpleMeasure2();
//        exampleBenchmarkSleep1();
//        exampleBenchmarkSleep2();
//        exampleBenchmarkBigDecimalDivide();
        exampleBenchmarkSleep2Dimensions();
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

    public static void exampleBenchmarkSleep2Dimensions() {
        new BenchmarkBuilder()
                .csvReport("example_sleep_2dim_0_to_50_0_to_50.csv")
                .allocatedMeasureSeconds(0.1)
                .timeUnit(TimeUnit.MilliSeconds)
                .forLoop(0, 50)
                .forLoop(0, 10)
                .benchmark("sleep", (millis1, millis2) -> {
                    try {
                        Thread.sleep(millis1 + millis2);
                    } catch (InterruptedException e) {
                    }
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
    }

    public static void calibrateBenchmarks() {
        MathContext mc = new MathContext(1000);
        new BenchmarkBuilder()
                .csvReport("calibrate_warmup.csv")
                .preWarmupCount(5)
                .measureFirstTimeOnly(true)
                .allocatedSleepSeconds(0.1)
                .resultCalculator(ResultCalculators.MEDIAN)
                .forLoop(0, 2000)
                .benchmark("piChudnovski", i -> {
                    piChudnovski(mc);
                })
                .run();
    }


    private static BigDecimal piChudnovski(MathContext mathContext) {
        MathContext mc = new MathContext(mathContext.getPrecision() + 10, mathContext.getRoundingMode());

        final BigDecimal value24 = BigDecimal.valueOf(24);
        final BigDecimal value640320 = BigDecimal.valueOf(640320);
        final BigDecimal value13591409 = BigDecimal.valueOf(13591409);
        final BigDecimal value545140134 = BigDecimal.valueOf(545140134);
        final BigDecimal valueDivisor = value640320.pow(3).divide(value24, mc);

        BigDecimal sumA = BigDecimal.ONE;
        BigDecimal sumB = BigDecimal.ZERO;

        BigDecimal a = BigDecimal.ONE;
        long dividendTerm1 = 5; // -(6*k - 5)
        long dividendTerm2 = -1; // 2*k - 1
        long dividendTerm3 = -1; // 6*k - 1
        BigDecimal kPower3 = BigDecimal.ZERO;

        long iterationCount = (mc.getPrecision()+13) / 14;
        for (long k = 1; k <= iterationCount; k++) {
            BigDecimal valueK = BigDecimal.valueOf(k);
            dividendTerm1 += -6;
            dividendTerm2 += 2;
            dividendTerm3 += 6;
            BigDecimal dividend = BigDecimal.valueOf(dividendTerm1).multiply(BigDecimal.valueOf(dividendTerm2)).multiply(BigDecimal.valueOf(dividendTerm3));
            kPower3 = valueK.pow(3);
            BigDecimal divisor = kPower3.multiply(valueDivisor, mc);
            a = a.multiply(dividend).divide(divisor, mc);
            BigDecimal b = valueK.multiply(a, mc);

            sumA = sumA.add(a);
            sumB = sumB.add(b);
        }

        final BigDecimal value426880 = BigDecimal.valueOf(426880);
        final BigDecimal value10005 = BigDecimal.valueOf(10005);
        final BigDecimal factor = value426880.multiply(BigDecimalMath.sqrt(value10005, mc));
        BigDecimal pi = factor.divide(value13591409.multiply(sumA, mc).add(value545140134.multiply(sumB, mc)), mc);

        return BigDecimalMath.round(pi, mathContext);
    }
}
