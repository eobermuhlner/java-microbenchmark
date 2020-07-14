package ch.obermuhlner.java.microbenchmark.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BenchmarkSuite {
    String value() default "";
    double allocatedSeconds() default 1.0;
}
