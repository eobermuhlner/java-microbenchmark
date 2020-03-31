package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.annotation.Benchmark;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkArgument;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkSuite;

import java.lang.reflect.*;
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

        BenchmarkSuite suiteAnnotation = clazz.getAnnotation(BenchmarkSuite.class);
        if (suiteAnnotation != null) {
            if (suiteAnnotation.value() != null && !suiteAnnotation.value().equals("")) {
                suiteName = suiteAnnotation.value();
            }
        }

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

        BenchmarkRunner benchmarkRunner = new BenchmarkRunner();
        benchmarkRunner.csvReport(suiteName + ".csv");

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

    @BenchmarkSuite
    public static class ExampleBenchmark {
        @BenchmarkArgument
        public int[] arguments = { 0, 10, 20, 30, 40, 50 };

        @Benchmark
        public void sleep(int millis) throws InterruptedException {
            Thread.sleep(millis);
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
        BenchmarkClassRunner.runClass(ExampleBenchmark2.class);
    }
}
