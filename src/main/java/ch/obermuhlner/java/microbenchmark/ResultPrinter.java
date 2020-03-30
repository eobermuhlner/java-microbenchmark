package ch.obermuhlner.java.microbenchmark;

import java.util.List;

public interface ResultPrinter extends AutoCloseable {
    void printNames(List<String> names);

    void printArguments(List<String> arguments);

    void printSuite(String name, String argument, double seconds);

    void printFinished();

    @Override
    void close();
}
