package edu.oswego.cs;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ParallelBenchmark {

    final private static int MAX_THREADS = 128;

    final private static int THREAD_COUNT_HIGH_READER_READERS = (int)(MAX_THREADS * 0.95);

    final private static int THREAD_COUNT_HIGH_READER_WRITERS = MAX_THREADS - THREAD_COUNT_HIGH_READER_READERS;

    final private static int THREAD_COUNT_MEDIUM_READER_READERS = (int)(MAX_THREADS * 0.75);

    final private static int THREAD_COUNT_MEDIUM_READER_WRITERS = MAX_THREADS - THREAD_COUNT_MEDIUM_READER_READERS;

    final private static int THREAD_COUNT_LOW_READER_READERS = (int)(MAX_THREADS * 0.5);

    final private static int THREAD_COUNT_LOW_READER_WRITERS = MAX_THREADS - THREAD_COUNT_LOW_READER_READERS;

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(2)
    public static class CustomLockingServer {
    
        LockingBankMap lbm;

        List<String> pregeneratedAccountIds;

        final int STARTING_ACCOUNT_AMOUNT = 1000;

        final int POSSIBLE_ACTIONS = 4;

        final double MAX_STARTING_AMOUNT = 1000;

        final double MAX_ACTION_AMOUNT = 500;

        @Setup(Level.Iteration)
        public void setup() {
            lbm = new LockingBankMap();
            pregeneratedAccountIds = Collections.synchronizedList(new ArrayList<>());
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < STARTING_ACCOUNT_AMOUNT; i++) {
                double checkingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
                double savingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
                String id = UUID.randomUUID().toString();
                lbm.put(id, new BankAccount(id, checkingsBalance, savingsBalance));
                pregeneratedAccountIds.add(id);
            }
        }

        public double doRandomAccountAction() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int randomAccount = random.nextInt(pregeneratedAccountIds.size());
            int randomAction = random.nextInt(POSSIBLE_ACTIONS);
            double actionAmount = random.nextDouble() * MAX_ACTION_AMOUNT;
            BankAccount account = lbm.getBankAccount(pregeneratedAccountIds.get(randomAccount));
            if (randomAction == 0) {
                return account.depositCheckings(actionAmount);
            } else if (randomAction == 1) {
                return account.depositSavings(actionAmount);
            } else if (randomAction == 2) {
                return account.transferCheckingsToSavings(actionAmount);
            } else {
                return account.transferSavingsToCheckings(actionAmount);
            }
        }

        public String addNewAccount() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double checkingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
            double savingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
            String accountId = UUID.randomUUID().toString();
            lbm.put(accountId, new BankAccount(accountId, checkingsBalance, savingsBalance));
            return accountId;
        }

        @Benchmark
        @Group("locking_high_readers")
        @GroupThreads(THREAD_COUNT_HIGH_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double lshReaderBenchmark(CustomLockingServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("locking_high_readers")
        @GroupThreads(THREAD_COUNT_HIGH_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String lshWriterBenchmark(CustomLockingServer server) throws InterruptedException {
            return server.addNewAccount();
        }

        @Benchmark
        @Group("locking_medium_readers")
        @GroupThreads(THREAD_COUNT_MEDIUM_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double lsmReaderBenchmark(CustomLockingServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("locking_medium_readers")
        @GroupThreads(THREAD_COUNT_MEDIUM_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String lsmWriterBenchmark(CustomLockingServer server) throws InterruptedException {
            return server.addNewAccount();
        }

        @Benchmark
        @Group("locking_low_readers")
        @GroupThreads(THREAD_COUNT_LOW_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double lslReaderBenchmark(CustomLockingServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("locking_low_readers")
        @GroupThreads(THREAD_COUNT_LOW_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String lslWriterBenchmark(CustomLockingServer server) throws InterruptedException {
            return server.addNewAccount();
        }

    }

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(2)
    public static class ConcurrentHashmapServer {

        ConcurrentHashMap<String, BankAccount> concurrentBankMap;

        List<String> pregeneratedAccountIds;

        final int STARTING_ACCOUNT_AMOUNT = 1000;

        final int POSSIBLE_ACTIONS = 4;

        final double MAX_STARTING_AMOUNT = 1000;

        final double MAX_ACTION_AMOUNT = 500;

        @Setup(Level.Iteration)
        public void setup() {
            concurrentBankMap = new ConcurrentHashMap<>();
            pregeneratedAccountIds = Collections.synchronizedList(new ArrayList<>());
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < STARTING_ACCOUNT_AMOUNT; i++) {
                double checkingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
                double savingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
                String id = UUID.randomUUID().toString();
                concurrentBankMap.put(id, new BankAccount(id, checkingsBalance, savingsBalance));
                pregeneratedAccountIds.add(id);
            }
        }

        public double doRandomAccountAction() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int randomAccount = random.nextInt(pregeneratedAccountIds.size());
            int randomAction = random.nextInt(POSSIBLE_ACTIONS);
            double actionAmount = random.nextDouble() * MAX_ACTION_AMOUNT;
            BankAccount account = concurrentBankMap.get(pregeneratedAccountIds.get(randomAccount));
            if (randomAction == 0) {
                return account.depositCheckings(actionAmount);
            } else if (randomAction == 1) {
                return account.depositSavings(actionAmount);
            } else if (randomAction == 2) {
                return account.transferCheckingsToSavings(actionAmount);
            } else {
                return account.transferSavingsToCheckings(actionAmount);
            }
        }

        public String addNewAccount() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double checkingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
            double savingsBalance = random.nextDouble() * MAX_STARTING_AMOUNT;
            String accountId = UUID.randomUUID().toString();
            concurrentBankMap.put(accountId, new BankAccount(accountId, checkingsBalance, savingsBalance));
            return accountId;
        }

        @Benchmark
        @Group("concurrent_hashmap_high_readers")
        @GroupThreads(THREAD_COUNT_HIGH_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double cshReaderBenchmark(ConcurrentHashmapServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("concurrent_hashmap_high_readers")
        @GroupThreads(THREAD_COUNT_HIGH_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String cshWriterBenchmark(ConcurrentHashmapServer server) throws InterruptedException {
            return server.addNewAccount();
        }

        @Benchmark
        @Group("concurrent_hashmap_medium_readers")
        @GroupThreads(THREAD_COUNT_MEDIUM_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double csmReaderBenchmark(ConcurrentHashmapServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("concurrent_hashmap_medium_readers")
        @GroupThreads(THREAD_COUNT_MEDIUM_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String csmWriterBenchmark(ConcurrentHashmapServer server) throws InterruptedException {
            return server.addNewAccount();
        }

        @Benchmark
        @Group("concurrent_hashmap_low_readers")
        @GroupThreads(THREAD_COUNT_LOW_READER_READERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double cslReaderBenchmark(ConcurrentHashmapServer server) {
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("concurrent_hashmap_low_readers")
        @GroupThreads(THREAD_COUNT_LOW_READER_WRITERS)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String cslWriterBenchmark(ConcurrentHashmapServer server) throws InterruptedException {
            return server.addNewAccount();
        }

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(".*" + ParallelBenchmark.class.getSimpleName() + ".*").build();
        new Runner(opt).run();
    }

}
