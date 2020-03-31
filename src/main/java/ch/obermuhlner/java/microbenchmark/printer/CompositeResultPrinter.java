package ch.obermuhlner.java.microbenchmark.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeResultPrinter implements ResultPrinter {

    private final List<ResultPrinter> printers = new ArrayList<>();

    public CompositeResultPrinter(ResultPrinter... printers) {
        this.printers.addAll(Arrays.asList(printers));
    }

    public void addResultPrinter(ResultPrinter resultPrinter) {
        printers.add(resultPrinter);
    }

    @Override
    public void printDimensions(int count) {
        for (ResultPrinter printer : printers) {
            printer.printDimensions(count);
        }
    }


    @Override
    public void printNames(List<String> names) {
        for (ResultPrinter printer : printers) {
            printer.printNames(names);
        }
    }

    @Override
    public void printArguments(List<String> arguments) {
        for (ResultPrinter printer : printers) {
            printer.printArguments(arguments);
        }
    }

    @Override
    public void printBenchmark(String name, String argument, double seconds) {
        for (ResultPrinter printer : printers) {
            printer.printBenchmark(name, argument, seconds);
        }
    }

    @Override
    public void printFinished() {
        for (ResultPrinter printer : printers) {
            printer.printFinished();
        }
    }

    @Override
    public void close() {
        for (ResultPrinter printer : printers) {
            printer.close();
        }
    }
}
