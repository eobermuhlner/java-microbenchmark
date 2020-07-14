package ch.obermuhlner.java.microbenchmark.printer;

import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvResultPrinter implements ResultPrinter {
    private final PrintWriter out;

    private String argumentName = "argument";

    private List<String> names;
    private List<String> arguments;

    private TimeUnit timeUnit;
    private Map<List<String>, Double> resultMap = new HashMap<>();

    public CsvResultPrinter(PrintWriter out) {
        this.out = out;
    }

    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public void printDimensions(int count) {
        switch (count) {
            case 1:
                if (timeUnit != null) {
                    out.println("# csv2chart.y-axis=" + timeUnit.longUnit);
                }
                break;
            case 2:
                out.println("# csv2chart.chart=heat");
                out.println("# csv2chart.header-column");
                out.println("# csv2chart.header-row");
                break;
        }
    }

    @Override
    public void printNames(List<String> names) {
        this.names = names;
    }

    @Override
    public void printArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void printInfoValue(String name, int value) {
    }

    @Override
    public void printInfoValue(String name, double value) {
    }

    @Override
    public void printBenchmark(String name, String argument, double elapsed, double[] allElapsed) {
        resultMap.put(Arrays.asList(name, argument), elapsed);
    }

    @Override
    public void printFinished() {
        out.print(String.format("%-40s", argumentName));
        for (String name : names) {
            out.print(", ");
            out.print(String.format("%16s", name));
        }
        out.println();

        for (String argument : arguments) {
            out.print(String.format("%-40s", argument));
            for (String name : names) {
                out.print(", ");
                Double seconds = resultMap.get(Arrays.asList(name, argument));
                if (seconds != null) {
                    out.print(String.format("%16.1f", seconds));
                }
            }
            out.println();
        }
    }

    @Override
    public void close() {
        out.close();
    }
}
