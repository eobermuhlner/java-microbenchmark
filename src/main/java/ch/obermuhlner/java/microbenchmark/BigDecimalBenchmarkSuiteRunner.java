package ch.obermuhlner.java.microbenchmark;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BigDecimalBenchmarkSuiteRunner extends BenchmarkSuiteRunner<BigDecimal> {

    public BigDecimalBenchmarkSuiteRunner() {
    }

    public BigDecimalBenchmarkSuiteRunner(ResultPrinter resultPrinter) {
        super(resultPrinter);
    }

    public BigDecimalBenchmarkSuiteRunner forLoop(BigDecimal startValue, BigDecimal exclEndValue) {
        return forLoop(startValue, exclEndValue, startValue.compareTo(exclEndValue) <= 0 ? BigDecimal.ONE : BigDecimal.ONE.negate());
    }

    public BigDecimalBenchmarkSuiteRunner forLoop(BigDecimal startValue, BigDecimal exclEndValue, BigDecimal step) {
        List<BigDecimal> arguments = new ArrayList<>();
        BigDecimal i = startValue;
        while (i.compareTo(exclEndValue) < 0) {
            arguments.add(i);
            i = i.add(step);
        }
        arguments(arguments);
        return this;
    }
}
