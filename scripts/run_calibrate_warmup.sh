#!/bin/sh

export JAVA_HOME="C:/Program Files/AdoptOpenJDK/jdk-11.0.5.10-hotspot/"
CSV2CHART=csv2chart


run_benchmark()
{
  test_name="$1"
  export CH_OBERMUHLNER_JAVA_MICROBENCHMARK_EXAMPLE_OPTS="-XX:+UnlockDiagnosticVMOptions -XX:CompilerDirectivesFile=${test_name}.json"
  ch.obermuhlner.java.microbenchmark.example/build/install/ch.obermuhlner.java.microbenchmark.example/bin/ch.obermuhlner.java.microbenchmark.example

  mv calibrate_warmup.csv calibrate_warmup_${test_name}.csv
}

run_benchmark_compile_all()
{
  test_name="$1"
  export CH_OBERMUHLNER_JAVA_MICROBENCHMARK_EXAMPLE_OPTS="-XX:+UnlockDiagnosticVMOptions -XX:CompilerDirectivesFile=${test_name}.json -Xcomp"
  ch.obermuhlner.java.microbenchmark.example/build/install/ch.obermuhlner.java.microbenchmark.example/bin/ch.obermuhlner.java.microbenchmark.example

  mv calibrate_warmup.csv calibrate_warmup_${test_name}_all.csv
}

./gradlew :ch.obermuhlner.java.microbenchmark.example:install

run_benchmark compile_nothing
run_benchmark compile_jit_c1
run_benchmark compile_jit_c2
run_benchmark compile_jit_c1_c2

run_benchmark_compile_all compile_jit_c1
run_benchmark_compile_all compile_jit_c2
run_benchmark_compile_all compile_jit_c1_c2

$CSV2CHART --format png *.csv