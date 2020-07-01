package ch.obermuhlner.java.microbenchmark.runner;

public enum TimeUnit {
    NanoSeconds("nanoseconds", "nanos", "ns", 1),
    MicroSeconds("microseconds", "micros", "μs", 1_000),
    MilliSeconds("milliseconds", "millis", "ms", 1_000_000),
    Seconds("seconds", "seconds", "s", 1_000_000_000);

    public final String longUnit;
    public final String shortUnit;
    public final String siUnit;
    private final double nanosPerUnit;

    TimeUnit(String longUnit, String shortUnit, String siUnit, double nanos) {
        this.longUnit = longUnit;
        this.shortUnit = shortUnit;
        this.siUnit = siUnit;
        this.nanosPerUnit = nanos;
    }

    public double toNanoSeconds(double time) {
        return time * nanosPerUnit;
    }

    public double nanosecondsToTimeUnit(double nanoseconds) {
        return nanoseconds / nanosPerUnit;
    }
}
