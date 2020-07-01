package ch.obermuhlner.java.microbenchmark.runner;

import ch.obermuhlner.java.microbenchmark.annotation.Benchmark;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkArgument;
import ch.obermuhlner.java.microbenchmark.annotation.BenchmarkSuite;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkRunnerOneArgument;
import ch.obermuhlner.java.microbenchmark.runner.internal.BenchmarkRunnerTwoArguments;

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

        BenchmarkRunner benchmark = new BenchmarkRunner();
        BenchmarkRunnerOneArgument benchmarkRunnerOneArgument = null;
        BenchmarkRunnerTwoArguments benchmarkRunnerTwoArguments = null;

        BenchmarkSuite suiteAnnotation = clazz.getAnnotation(BenchmarkSuite.class);
        if (suiteAnnotation != null) {
            if (suiteAnnotation.value() != null && !suiteAnnotation.value().equals("")) {
                suiteName = suiteAnnotation.value();
            }
            benchmark.allocatedMeasureSeconds(suiteAnnotation.allocatedSeconds());
        }

        benchmark.csvReport(suiteName + ".csv");

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
                benchmarkRunnerOneArgument = benchmark.forArguments((List) argument);
            } else if (argument instanceof Stream) {
                benchmarkRunnerOneArgument = benchmark.forArguments((Stream) argument);
            } else if (argument instanceof Object[]) {
                benchmarkRunnerOneArgument = benchmark.forArguments((Object[]) argument);
            } else if (argument.getClass().isArray()) {
                List<Object> objectArguments = new ArrayList<>();
                int length = Array.getLength(argument);
                for (int i = 0; i < length; i ++) {
                    objectArguments.add(Array.get(argument, i));
                }
                benchmarkRunnerOneArgument = benchmark.forArguments(objectArguments);
            } else {
                // TODO ignore or exception?
            }
        }

        for (Method method : clazz.getMethods()) {
            Benchmark annotation = method.getDeclaredAnnotation(Benchmark.class);
            if (annotation != null) {
                switch(sortIndexes.size()) {
                    case 1:
                        benchmarkRunnerOneArgument.benchmark(method.getName(), arg -> {
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
                        benchmarkRunnerTwoArguments.benchmark(method.getName(), (arg1, arg2) -> {
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

        if (benchmarkRunnerTwoArguments != null) {
            benchmarkRunnerTwoArguments.run();
        } else if (benchmarkRunnerOneArgument != null) {
            benchmarkRunnerOneArgument.run();
        } else {
            throw new RuntimeException("No benchmarks to run");
        }
    }
}
