package edu.oswego.cs;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ParallelBenchmark {

    LockingCourseMap lcm;

    ConcurrentHashMap<String,Course> chm;

    @Setup
    public void setup() {
        lcm = new LockingCourseMap();
        chm = new ConcurrentHashMap<>();
    }

    @Benchmark
    public void testMethod() {
        
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().forks(1).build();
        new Runner(opt).run();
    }

}
