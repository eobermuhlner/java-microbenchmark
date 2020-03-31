package ch.obermuhlner.java.microbenchmark.printer;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class SimpleResultPrinter implements ResultPrinter {

    private final PrintStream out;
    private final boolean doClose;

    public SimpleResultPrinter() {
        this(System.out, false);
    }

    public SimpleResultPrinter(String fileName) throws FileNotFoundException {
        this(new PrintStream(fileName));
    }

    public SimpleResultPrinter(PrintStream out) {
        this(out, true);
    }

    public SimpleResultPrinter(PrintStream out, boolean doClose) {
        this.out = out;
        this.doClose = doClose;
    }

    @Override
    public void printDimensions(int count) {
    }

    @Override
    public void printNames(List<String> names) {
    }

    @Override
    public void printArguments(List<String> arguments) {
    }

    @Override
    public void printBenchmark(String name, String argument, double seconds) {
        out.println(String.format("%-40s %30s %16.1f", name, argument, seconds));
    }

    @Override
    public void printFinished() {
    }

    @Override
    public void close() {
        if (doClose) {
            out.close();
        }
    }
}
