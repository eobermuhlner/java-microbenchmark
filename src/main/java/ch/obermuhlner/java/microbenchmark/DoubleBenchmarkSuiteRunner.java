package ch.obermuhlner.java.microbenchmark;

import java.util.ArrayList;
import java.util.List;

public class DoubleBenchmarkSuiteRunner extends BenchmarkSuiteRunner<Double> {

    public DoubleBenchmarkSuiteRunner() {
    }

    public DoubleBenchmarkSuiteRunner(ResultPrinter resultPrinter) {
        super(resultPrinter);
    }

    public DoubleBenchmarkSuiteRunner forLoop(double startValue, double exclEndValue) {
        return forLoop(startValue, exclEndValue, startValue <= exclEndValue ? 1 : -1);
    }

    public DoubleBenchmarkSuiteRunner forLoop(double startValue, double exclEndValue, double step) {
        List<Double> arguments = new ArrayList<>();
        for (double i = startValue; i < exclEndValue; i+=step) {
            arguments.add(i);
        }
        arguments(arguments);
        return this;
    }
}
