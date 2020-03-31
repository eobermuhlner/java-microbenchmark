package ch.obermuhlner.java.microbenchmark.printer;

import java.util.List;

public interface ResultPrinter extends AutoCloseable {
    void printDimensions(int count);

    void printNames(List<String> names);

    void printArguments(List<String> arguments);

    void printBenchmark(String name, String argument, double seconds);

    void printFinished();

    @Override
    void close();
}
