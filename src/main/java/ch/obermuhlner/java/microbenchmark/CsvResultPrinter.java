package ch.obermuhlner.java.microbenchmark;

import java.io.PrintWriter;
import java.util.List;

public class CsvResultPrinter implements ResultPrinter {
    private final PrintWriter out;

    private String argumentName = "argument";

    private List<String> names;
    private List<String> arguments;

    public CsvResultPrinter() {
        this.out = new PrintWriter(System.out);
    }

    public CsvResultPrinter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void printNames(List<String> names) {
        this.names = names;

        out.print(argumentName);
        for (int i = 0; i < names.size(); i++) {
            out.print(", ");
            out.print(names.get(i));
        }
        out.println();
    }

    @Override
    public void printArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void printSuite(String name, String argument, double seconds) {
        if (name.equals(names.get(0))) {
            out.print(argument);
        }

        out.print(", ");
        out.print(seconds);

        if (name.equals(names.get(names.size() - 1))) {
            out.println();
        }
    }
}
