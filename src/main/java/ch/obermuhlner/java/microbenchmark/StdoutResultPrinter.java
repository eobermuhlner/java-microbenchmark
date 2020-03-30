package ch.obermuhlner.java.microbenchmark;

import java.util.List;

public class StdoutResultPrinter implements ResultPrinter {

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
        System.out.println(String.format("%-40s %30s %16.1f", name, argument, seconds));
    }

    @Override
    public void printFinished() {
    }

    @Override
    public void close() {
    }
}
