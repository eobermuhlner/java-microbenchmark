package ch.obermuhlner.java.microbenchmark;

import java.util.List;

public interface ResultPrinter {
    void printNames(List<String> names);

    void printArguments(List<String> arguments);

    void printSuite(String name, String argument, double seconds);
}
