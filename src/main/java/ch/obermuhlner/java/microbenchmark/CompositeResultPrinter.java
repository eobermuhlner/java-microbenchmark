package ch.obermuhlner.java.microbenchmark;

import java.util.List;

public class CompositeResultPrinter implements ResultPrinter {

    private final ResultPrinter[] printers;

    public CompositeResultPrinter(ResultPrinter... printers) {
        this.printers = printers;
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
    public void printSuite(String name, String argument, double seconds) {
        for (ResultPrinter printer : printers) {
            printer.printSuite(name, argument, seconds);
        }
    }

    @Override
    public void printFinished() {
        for (ResultPrinter printer : printers) {
            printer.printFinished();
        }
    }
}
