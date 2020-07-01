package ch.obermuhlner.java.microbenchmark.printer;

import ch.obermuhlner.java.microbenchmark.runner.TimeUnit;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class SimpleResultPrinter implements ResultPrinter {

    private final PrintStream out;
    private final boolean doClose;
    private boolean verbose = false;
    private TimeUnit timeUnit;

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

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
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
    public void printInfoValue(String name, int value) {
        if (verbose) {
            out.println(String.format("%40s %30s %16s %s=%d", "", "", "", name, value));
        }
    }

    @Override
    public void printInfoValue(String name, double value) {
        if (verbose) {
            out.println(String.format("%40s %30s %16s %s=%f", "", "", "", name, value));
        }
    }

    @Override
    public void printBenchmark(String name, String argument, double elapsed, double[] allElapsed) {
        int n = allElapsed.length;
        double sum = 0;
        for (double value : allElapsed) {
            sum += value;
        }

        Arrays.sort(allElapsed);
        double min = allElapsed[0];
        double max = allElapsed[n-1];
        double avg = sum / n;
        double median;
        if (n % 2 == 0) {
            median = (allElapsed[n/2-1] + allElapsed[n/2]) / 2;
        } else {
            median = allElapsed[n/2];
        }

        double sumDiffSquare = 0;
        for (double value : allElapsed) {
            double diff = value - avg;
            sumDiffSquare += diff*diff;
        }
        double stddev = Math.sqrt(sumDiffSquare/(n+1));

        out.println(String.format("%-40s %30s %16.1f %8s (n=%d min=%f max=%f avg=%f median=%f stddev=%f)", name, argument, elapsed, timeUnit.shortUnit, n, min, max, avg, median, stddev));
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
