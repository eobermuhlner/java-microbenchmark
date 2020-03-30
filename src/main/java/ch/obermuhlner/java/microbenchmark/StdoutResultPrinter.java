package ch.obermuhlner.java.microbenchmark;

import java.util.List;

public class StdoutResultPrinter implements ResultPrinter {

    @Override
    public void printNames(List<String> names) {
    }

    @Override
    public void printArguments(List<String> arguments) {
    }

    @Override
    public void printSuite(String name, String argument, double seconds) {
        System.out.println(String.format("%-40s %30s %10.6f", name, argument, seconds));
    }

    @Override
    public void printFinished() {
    }

    @Override
    public void close() {
    }
}
