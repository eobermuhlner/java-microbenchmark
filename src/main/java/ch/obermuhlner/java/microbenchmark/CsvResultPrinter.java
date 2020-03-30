package ch.obermuhlner.java.microbenchmark;

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

    private Map<List<String>, Double> resultMap = new HashMap<>();

    public CsvResultPrinter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void printDimensions(int count) {
        switch (count) {
            case 1:
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
    public void printBenchmark(String name, String argument, double seconds) {
        resultMap.put(Arrays.asList(name, argument), seconds);
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
