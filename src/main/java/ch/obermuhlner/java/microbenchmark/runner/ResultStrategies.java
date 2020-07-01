package ch.obermuhlner.java.microbenchmark.runner;

import java.util.Arrays;
import java.util.function.Function;

public class ResultStrategies {
    public static Function<double[], Double> MIN = values -> {
        if (values.length == 0) {
            return 0.0;
        }

        double result = Double.MAX_VALUE;
        for (double value : values) {
            result = Math.min(result, value);
        }
        return result;
    };

    public static Function<double[], Double> AVERAGE = values -> {
        if (values.length == 0) {
            return 0.0;
        }

        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    };

    public static Function<double[], Double> MEDIAN = values -> {
        if (values.length == 0) {
            return 0.0;
        }

        Arrays.sort(values);

        int n = values.length;
        if (n % 2 == 0) {
            return (values[n/2-1] + values[n/2]) / 2;
        } else {
            return values[n/2];
        }
    };

    public static Function<double[], Double> AVERAGE_LOWER_HALF = values -> {
        if (values.length == 0) {
            return 0.0;
        }
        if (values.length == 1) {
            return values[0];
        }

        Arrays.sort(values);

        int n = values.length / 2;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += values[i];
        }
        return sum / n;
    };
}
