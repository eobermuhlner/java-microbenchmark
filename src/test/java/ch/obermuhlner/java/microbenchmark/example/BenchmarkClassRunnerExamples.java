package ch.obermuhlner.java.microbenchmark.example;

import ch.obermuhlner.java.microbenchmark.annotation.Benchmark;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkArgument;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkSuite;
import ch.obermuhlner.java.microbenchmark.runner.BenchmarkClassRunner;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BenchmarkClassRunnerExamples {

    @BenchmarkSuite(allocatedSeconds = 0.1)
    public static class ExampleBenchmark {
        private BigDecimal value1 = BigDecimal.valueOf(1.23456);
        private BigDecimal value2 = BigDecimal.valueOf(9.87654);
        @BenchmarkArgument
        public List<BigDecimal> arguments2() {
            List<BigDecimal> result = new ArrayList<>();
            BigDecimal i = BigDecimal.valueOf(0);
            while (i.compareTo(BigDecimal.valueOf(100)) < 0) {
                result.add(i);
                i = i.add(BigDecimal.valueOf(1.0));
            }
            return result;
        }

        @Benchmark
        public void divide(BigDecimal x) {
            value2.divide(value1, MathContext.DECIMAL128);
        }
    }


    @BenchmarkSuite
    public static class ExampleBenchmark2 {
        @BenchmarkArgument(1)
        public List<Integer> arguments1 = Arrays.asList(0, 1, 2, 3, 4);

        @BenchmarkArgument(2)
        public List<Integer> arguments2() {
            return Arrays.asList(0, 10, 20);
        }

        @Benchmark
        public void sleep(int millis1, int millis2) throws InterruptedException {
            Thread.sleep(millis1 + millis2);
        }
    }

    public static void main(String[] args) {
        BenchmarkClassRunner.runClass(ExampleBenchmark.class);
        //BenchmarkClassRunner.runClass(ExampleBenchmark2.class);
    }
}
