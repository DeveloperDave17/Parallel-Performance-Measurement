package edu.oswego.cs;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ParallelBenchmark {

    final private static int threadMoreReaderCount = (int)(Threads.MAX * 0.8);

    final private static int threadLessWriterCount = Threads.MAX - threadMoreReaderCount;

    @State(Scope.Group)
    @Threads(Threads.MAX)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
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
            System.out.println(1);
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
        @Group("locking_more_readers")
        @GroupThreads(threadMoreReaderCount)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static double lcmReaderBenchmark(CustomLockingServer server) {
            System.out.println(1);
            return server.doRandomAccountAction();
        }

        @Benchmark
        @Group("locking_more_readers")
        @GroupThreads(threadLessWriterCount)
        @Warmup(iterations = 3)
        @Measurement(iterations = 5)
        public static String lcmWriterBenchmark(CustomLockingServer server) throws InterruptedException {
            System.out.println(2);
            return server.addNewAccount();
        }


    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(ParallelBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }

}
