package com.gym.concurrent;

import com.gym.model.Attendance;
import com.gym.model.Payment;
import com.gym.service.GymService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent attendance / payment processing.
 *
 *  - ExecutorService : fixed thread pool that runs the batch jobs
 *  - Callable/Future : each job returns a result (Attendance or Payment) or an exception
 *  - Runnable/Thread : {@link SummaryTask} is run on its own Thread
 */
public class ConcurrentProcessor implements AutoCloseable {

    private final GymService service;
    private final ExecutorService executor;

    public ConcurrentProcessor(GymService service, int threads) {
        this.service = service;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    /** Check in many members at the same time. */
    public BatchResult<Attendance> processAttendance(List<String> memberIds) {
        Map<String, Callable<Attendance>> tasks = new LinkedHashMap<>();
        for (String id : new LinkedHashSet<>(memberIds)) {
            tasks.put(id, () -> {
                log("check-in   " + id);
                return service.recordAttendance(id);
            });
        }
        return runAll("attendance", tasks);
    }

    /** Collect monthly fees for many members at the same time. */
    public BatchResult<Payment> processPayments(List<String> memberIds) {
        Map<String, Callable<Payment>> tasks = new LinkedHashMap<>();
        for (String id : new LinkedHashSet<>(memberIds)) {
            tasks.put(id, () -> {
                log("payment    " + id);
                return service.recordMonthlyPayment(id);
            });
        }
        return runAll("payments", tasks);
    }

    /** Runs a Runnable on a plain Thread and waits for it to finish. */
    public void printSummaryInBackground() {
        Thread thread = new Thread(new SummaryTask(service), "summary-thread");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private <T> BatchResult<T> runAll(String label, Map<String, Callable<T>> tasks) {
        List<String> keys = new ArrayList<>(tasks.keySet());
        List<Callable<T>> callables = new ArrayList<>(tasks.values());
        List<T> succeeded = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        try {
            List<Future<T>> futures = executor.invokeAll(callables);   // blocks until all finish
            for (int i = 0; i < futures.size(); i++) {
                try {
                    succeeded.add(futures.get(i).get());
                } catch (ExecutionException e) {
                    failures.add(keys.get(i) + ": " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failures.add(label + " batch interrupted");
        }
        return new BatchResult<>(succeeded, failures);
    }

    private static void log(String message) {
        System.out.println("    [" + Thread.currentThread().getName() + "] " + message);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** A Runnable that prints a short summary of the gym's current state. */
    static class SummaryTask implements Runnable {
        private final GymService service;

        SummaryTask(GymService service) {
            this.service = service;
        }

        @Override
        public void run() {
            System.out.printf("    [%s] Summary: %d members, %d check-ins, %d payments, revenue %.2f%n",
                    Thread.currentThread().getName(),
                    service.getAllMembers().size(),
                    service.getAllAttendance().size(),
                    service.getAllPayments().size(),
                    service.totalRevenue());
        }
    }
}
