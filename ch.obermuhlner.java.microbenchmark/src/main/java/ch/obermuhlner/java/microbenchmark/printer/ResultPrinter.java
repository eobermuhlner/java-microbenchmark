package ch.obermuhlner.java.microbenchmark.printer;

import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.util.List;

public interface ResultPrinter extends AutoCloseable {
    void setTimeUnit(TimeUnit timeUnit);

    void printDimensions(int count);

    void printNames(List<String> names);

    void printArguments(List<String> arguments);

    void printInfoValue(String name, int value);
    void printInfoValue(String name, double value);

    void printBenchmark(String name, String argument, double elapsed, double[] allElapsed);

    void printFinished();

    @Override
    void close();
}
