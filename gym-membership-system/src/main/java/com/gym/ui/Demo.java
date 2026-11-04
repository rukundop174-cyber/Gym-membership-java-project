package com.gym.ui;

import com.gym.concurrent.BatchResult;
import com.gym.concurrent.ConcurrentProcessor;
import com.gym.model.Attendance;
import com.gym.model.Member;
import com.gym.model.Payment;
import com.gym.service.GymService;
import com.gym.storage.GymData;
import com.gym.storage.JsonStorage;
import com.gym.storage.ReportWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Non-interactive walkthrough of every feature: run with  --demo  (good for screenshots / GitHub evidence). */
public class Demo {

    private final GymService service;
    private final JsonStorage storage;

    public Demo(GymService service, JsonStorage storage) {
        this.service = service;
        this.storage = storage;
    }

    public void run() throws IOException {
        heading("1. CO1 - OOP model: sample data (inheritance, overriding, toString)");
        SampleData.load(service);
        service.getAllPlans().forEach(p -> System.out.println("  " + p));
        service.getAllTrainers().forEach(t -> System.out.println("  " + t));
        service.getAllMembers().forEach(m -> System.out.println("  " + m));

        heading("2. Collections & generics: search and sort");
        System.out.println("  Search \"an\":");
        service.searchMembers("an").forEach(m -> System.out.println("    " + m.getId() + " " + m.getName()));
        System.out.println("  Members A-Z (Comparable):");
        service.getMembersByName().forEach(m -> System.out.println("    " + m.getName()));
        System.out.println("  Members by plan fee, high to low (Comparator):");
        service.getMembersByPlanFeeDescending().forEach(m -> System.out.println("    " + m.getName() + " (" + m.getPlanId() + ")"));

        heading("3. Concurrency: ExecutorService, Callable, Runnable, Thread");
        List<String> ids = service.getAllMembers().stream().map(Member::getId).collect(Collectors.toCollection(ArrayList::new));
        ids.add("M999");   // does not exist -> shows how failures are reported
        try (ConcurrentProcessor processor = new ConcurrentProcessor(service, 4)) {
            BatchResult<Attendance> attendance = processor.processAttendance(ids);
            System.out.println("  Check-ins recorded: " + attendance.succeeded().size());
            attendance.failures().forEach(f -> System.out.println("  failed -> " + f));

            BatchResult<Payment> payments = processor.processPayments(ids);
            System.out.println("  Payments recorded: " + payments.succeeded().size());
            payments.failures().forEach(f -> System.out.println("  failed -> " + f));

            processor.printSummaryInBackground();
        }

        heading("4. File I/O & Jackson JSON: save, load, verify");
        Path file = Paths.get("data", "demo-gym-data.json");
        storage.save(service.snapshot(), file);
        System.out.println("  Saved   -> " + file.toAbsolutePath());

        GymService reloaded = new GymService();
        GymData data = storage.load(file);
        reloaded.restore(data);
        boolean same = reloaded.getAllMembers().equals(service.getAllMembers())
                && reloaded.getAllPayments().size() == service.getAllPayments().size()
                && reloaded.getAllAttendance().size() == service.getAllAttendance().size()
                && Math.abs(reloaded.totalRevenue() - service.totalRevenue()) < 0.0001;
        System.out.println("  Reloaded: " + reloaded.getAllMembers().size() + " members, "
                + reloaded.getAllPayments().size() + " payments, "
                + reloaded.getAllAttendance().size() + " check-ins");
        System.out.println("  Data identical after reload? " + (same ? "YES" : "NO"));

        Path report = Paths.get("data", "demo-report.txt");
        new ReportWriter().write(reloaded, report);
        System.out.println("  Report  -> " + report.toAbsolutePath());
    }

    private static void heading(String text) {
        System.out.println();
        System.out.println("=== " + text + " ===");
    }
}
