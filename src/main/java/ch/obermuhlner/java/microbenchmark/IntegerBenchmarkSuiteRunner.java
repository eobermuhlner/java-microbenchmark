package ch.obermuhlner.java.microbenchmark;

import java.util.ArrayList;
import java.util.List;

public class IntegerBenchmarkSuiteRunner extends BenchmarkSuiteRunner<Integer> {

    public IntegerBenchmarkSuiteRunner() {
    }

    public IntegerBenchmarkSuiteRunner(ResultPrinter resultPrinter) {
        super(resultPrinter);
    }

    public IntegerBenchmarkSuiteRunner forLoop(int startValue, int exclEndValue) {
        return forLoop(startValue, exclEndValue, startValue <= exclEndValue ? 1 : -1);
    }

    public IntegerBenchmarkSuiteRunner forLoop(int startValue, int exclEndValue, int step) {
        List<Integer> arguments = new ArrayList<>();
        for (int i = startValue; i < exclEndValue; i+=step) {
            arguments.add(i);
        }
        arguments(arguments);
        return this;
    }
}
