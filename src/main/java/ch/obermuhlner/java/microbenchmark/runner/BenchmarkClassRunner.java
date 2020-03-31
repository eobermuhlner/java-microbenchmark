package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.annotation.Benchmark;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkArgument;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkSuite;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Stream;

public class BenchmarkClassRunner {
    public static <C> void runClass(Class<C> clazz) {
        try {
            runClassInternal(clazz);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
    }

    private static <C> void runClassInternal(Class<C> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String suiteName = clazz.getSimpleName();

        BenchmarkRunner benchmarkRunner = new BenchmarkRunner();

        BenchmarkSuite suiteAnnotation = clazz.getAnnotation(BenchmarkSuite.class);
        if (suiteAnnotation != null) {
            if (suiteAnnotation.value() != null && !suiteAnnotation.value().equals("")) {
                suiteName = suiteAnnotation.value();
            }
            benchmarkRunner.allocatedMeasureSeconds(suiteAnnotation.allocatedSeconds());
        }

        benchmarkRunner.csvReport(suiteName + ".csv");

        C instance;
        Constructor<C> constructor = clazz.getConstructor();
        instance = constructor.newInstance();

        Map<Integer, Object> indexToArgumentsMap = new HashMap<>();

        for (Field field : clazz.getFields()) {
            BenchmarkArgument annotation = field.getDeclaredAnnotation(BenchmarkArgument.class);
            if (annotation != null) {
                Object argument = field.get(instance);
                indexToArgumentsMap.put(annotation.value(), argument);
            }
        }

        for (Method method : clazz.getMethods()) {
            BenchmarkArgument annotation = method.getDeclaredAnnotation(BenchmarkArgument.class);
            if (annotation != null) {
                Object argument = method.invoke(instance);
                indexToArgumentsMap.put(annotation.value(), argument);
            }
        }

        List<Integer> sortIndexes = new ArrayList<>(indexToArgumentsMap.keySet());
        Collections.sort(sortIndexes);

        for (int argumentIndex : sortIndexes) {
            Object argument = indexToArgumentsMap.get(argumentIndex);

            if (argument == null) {
                // ignore
            } else if (argument instanceof List) {
                benchmarkRunner.forArguments((List) argument);
            } else if (argument instanceof Stream) {
                benchmarkRunner.forStream((Stream) argument);
            } else if (argument instanceof Object[]) {
                benchmarkRunner.forArguments((Object[]) argument);
            } else if (argument.getClass().isArray()) {
                List<Object> objectArguments = new ArrayList<>();
                int length = Array.getLength(argument);
                for (int i = 0; i < length; i ++) {
                    objectArguments.add(Array.get(argument, i));
                }
                benchmarkRunner.forArguments(objectArguments);
            } else {
                // TODO ignore or exception?
            }
        }

        for (Method method : clazz.getMethods()) {
            Benchmark annotation = method.getDeclaredAnnotation(Benchmark.class);
            if (annotation != null) {
                switch(sortIndexes.size()) {
                    case 1:
                        benchmarkRunner.benchmark(method.getName(), arg -> {
                            try {
                                method.invoke(instance, arg);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case 2:
                        benchmarkRunner.benchmark(method.getName(), (arg1, arg2) -> {
                            try {
                                method.invoke(instance, arg1, arg2);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                }
            }
        }

        benchmarkRunner.run();
    }

    @BenchmarkSuite(allocatedSeconds = 1)
    public static class ExampleBenchmark {
        private BigDecimal value = BigDecimal.valueOf(1.23456);
        private BigDecimal value2 = BigDecimal.valueOf(9.87654);
        @BenchmarkArgument
        public List<BigDecimal> arguments2() {
            List<BigDecimal> result = new ArrayList<>();
            BigDecimal i = BigDecimal.valueOf(0);
            while (i.compareTo(BigDecimal.valueOf(100)) < 0) {
                result.add(i);
                i = i.add(BigDecimal.valueOf(0.1));
            }
            return result;
        }

        @Benchmark
        public void divide(BigDecimal x) throws InterruptedException {
            value2.divide(value, MathContext.DECIMAL128);
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
