package com.gym.ui;

import com.gym.concurrent.BatchResult;
import com.gym.concurrent.ConcurrentProcessor;
import com.gym.model.Attendance;
import com.gym.model.Member;
import com.gym.model.MembershipPlan;
import com.gym.model.Payment;
import com.gym.model.Trainer;
import com.gym.service.GymService;
import com.gym.storage.GymData;
import com.gym.storage.JsonStorage;
import com.gym.storage.ReportWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Console menu: the main controller that ties all modules together. */
public class MenuController {

    private static final Path REPORT_FILE = Paths.get("data", "gym-report.txt");

    private final GymService service;
    private final JsonStorage storage;
    private final Path dataFile;
    private final Scanner in;

    public MenuController(GymService service, JsonStorage storage, Path dataFile, Scanner in) {
        this.service = service;
        this.storage = storage;
        this.dataFile = dataFile;
        this.in = in;
    }

    public void run() {
        System.out.println("=== Gym Membership Management System ===");
        autoLoad();

        boolean running = true;
        while (running) {
            printMenu();
            if (!in.hasNextLine()) {
                break;                      // input closed
            }
            String choice = in.nextLine().trim();
            try {
                running = handle(choice);
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            } catch (IOException e) {
                System.out.println("  ! File error: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                break;                      // input closed in the middle of a prompt
            }
        }
        System.out.println("Goodbye!");
    }

    private boolean handle(String choice) throws IOException {
        switch (choice) {
            case "1" -> addPlan();
            case "2" -> addMember();
            case "3" -> addTrainer();
            case "4" -> assignTrainer();
            case "5" -> { printMembers(service.getAllMembers()); }
            case "6" -> searchMembers();
            case "7" -> sortMembers();
            case "8" -> printPlansAndTrainers();
            case "9" -> checkIn();
            case "10" -> pay();
            case "11" -> concurrentAttendance();
            case "12" -> concurrentPayments();
            case "13" -> printPayments();
            case "14" -> saveJson();
            case "15" -> loadJson();
            case "16" -> exportReport();
            case "17" -> loadSample();
            case "0" -> { return confirmExit(); }
            default -> System.out.println("  ! Unknown option.");
        }
        return true;
    }

    // ------------------------------------------------------------------ menu
    private void printMenu() {
        System.out.println();
        System.out.println("--- MAIN MENU ---");
        System.out.println(" 1. Add membership plan       10. Record payment (one member)");
        System.out.println(" 2. Add member                11. Concurrent check-ins (ExecutorService)");
        System.out.println(" 3. Add trainer               12. Concurrent payments (ExecutorService)");
        System.out.println(" 4. Assign trainer to member  13. View payments");
        System.out.println(" 5. List members              14. Save data to JSON");
        System.out.println(" 6. Search members            15. Load data from JSON");
        System.out.println(" 7. Sort members              16. Export text report");
        System.out.println(" 8. View plans & trainers     17. Load sample data");
        System.out.println(" 9. Record check-in           0. Exit");
        System.out.print("Choose: ");
    }

    // --------------------------------------------------------------- actions
    private void addPlan() {
        String name = ask("Plan name: ");
        double fee = askDouble("Monthly fee: ");
        int months = askInt("Duration (months): ");
        System.out.println("  Added " + service.addPlan(name, fee, months));
    }

    private void addMember() {
        printPlansAndTrainers();
        String name = ask("Member name: ");
        String phone = ask("Phone: ");
        String email = ask("Email: ");
        String planId = ask("Plan id (e.g. P001): ").toUpperCase();
        System.out.println("  Added " + service.addMember(name, phone, email, planId));
    }

    private void addTrainer() {
        String name = ask("Trainer name: ");
        String phone = ask("Phone: ");
        String email = ask("Email: ");
        String spec = ask("Specialization: ");
        System.out.println("  Added " + service.addTrainer(name, phone, email, spec));
    }

    private void assignTrainer() {
        String memberId = ask("Member id: ").toUpperCase();
        String trainerId = ask("Trainer id: ").toUpperCase();
        service.assignTrainer(memberId, trainerId);
        System.out.println("  Trainer " + trainerId + " assigned to " + memberId + ".");
    }

    private void searchMembers() {
        String keyword = ask("Search (id / name / phone / email): ");
        List<Member> results = service.searchMembers(keyword);
        System.out.println("  " + results.size() + " result(s):");
        printMembers(results);
    }

    private void sortMembers() {
        System.out.println("  1. Name (A-Z)   2. Join date   3. Plan fee (high to low)");
        String c = ask("Sort by: ");
        switch (c) {
            case "1" -> printMembers(service.getMembersByName());
            case "2" -> printMembers(service.getMembersByJoinDate());
            case "3" -> printMembers(service.getMembersByPlanFeeDescending());
            default -> System.out.println("  ! Unknown sort option.");
        }
    }

    private void printPlansAndTrainers() {
        System.out.println("  Plans (cheapest first):");
        service.getPlansByFee().forEach(p -> System.out.println("    " + p));
        System.out.println("  Trainers:");
        service.getAllTrainers().forEach(t -> System.out.println("    " + t));
    }

    private void checkIn() {
        Attendance a = service.recordAttendance(ask("Member id: ").toUpperCase());
        System.out.println("  Recorded " + a);
    }

    private void pay() {
        Payment p = service.recordMonthlyPayment(ask("Member id: ").toUpperCase());
        System.out.println("  Recorded " + p);
    }

    private void concurrentAttendance() {
        List<String> ids = askMemberIds();
        try (ConcurrentProcessor processor = new ConcurrentProcessor(service, 4)) {
            BatchResult<Attendance> result = processor.processAttendance(ids);
            System.out.println("  Check-ins recorded: " + result.succeeded().size());
            result.failures().forEach(f -> System.out.println("  ! failed -> " + f));
            processor.printSummaryInBackground();
        }
    }

    private void concurrentPayments() {
        List<String> ids = askMemberIds();
        try (ConcurrentProcessor processor = new ConcurrentProcessor(service, 4)) {
            BatchResult<Payment> result = processor.processPayments(ids);
            System.out.println("  Payments recorded: " + result.succeeded().size());
            result.failures().forEach(f -> System.out.println("  ! failed -> " + f));
            processor.printSummaryInBackground();
        }
    }

    private void printPayments() {
        List<Payment> payments = service.getPaymentsByAmountDescending();
        if (payments.isEmpty()) {
            System.out.println("  (no payments yet)");
            return;
        }
        payments.forEach(p -> System.out.println("  " + p));
        System.out.printf("  Total revenue: %.2f%n", service.totalRevenue());
    }

    private void saveJson() throws IOException {
        storage.save(service.snapshot(), dataFile);
        System.out.println("  Saved to " + dataFile.toAbsolutePath());
    }

    private void loadJson() throws IOException {
        GymData data = storage.load(dataFile);
        service.restore(data);
        System.out.printf("  Loaded %d plans, %d members, %d trainers, %d check-ins, %d payments.%n",
                data.getPlans().size(), data.getMembers().size(), data.getTrainers().size(),
                data.getAttendance().size(), data.getPayments().size());
    }

    private void exportReport() throws IOException {
        new ReportWriter().write(service, REPORT_FILE);
        System.out.println("  Report written to " + REPORT_FILE.toAbsolutePath());
    }

    private void loadSample() {
        if (!service.getAllPlans().isEmpty() || !service.getAllMembers().isEmpty()) {
            System.out.println("  ! Sample data can only be loaded into an empty system.");
            return;
        }
        SampleData.load(service);
        System.out.println("  Sample data loaded.");
    }

    private boolean confirmExit() throws IOException {
        String answer = ask("Save data before exiting? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            saveJson();
        }
        return false;
    }

    private void autoLoad() {
        if (Files.exists(dataFile)) {
            try {
                loadJson();
            } catch (IOException e) {
                System.out.println("  ! Could not load saved data: " + e.getMessage());
            }
        } else {
            System.out.println("(No saved data found. Use option 17 to load sample data.)");
        }
    }

    // --------------------------------------------------------------- helpers
    private List<String> askMemberIds() {
        String text = ask("Member ids separated by commas (blank = all members): ");
        if (text.isBlank()) {
            return service.getAllMembers().stream().map(Member::getId).collect(Collectors.toList());
        }
        return new ArrayList<>(Arrays.stream(text.split(","))
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
    }

    private void printMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        System.out.printf("  %-6s %-18s %-12s %-10s %-11s %-8s %-8s%n",
                "ID", "Name", "Phone", "Plan", "Joined", "Trainer", "Visits");
        for (Member m : members) {
            String plan = service.findPlan(m.getPlanId()).map(MembershipPlan::getName).orElse("?");
            String trainer = m.getTrainerId() == null ? "-"
                    : service.findTrainer(m.getTrainerId()).map(Trainer::getName).orElse(m.getTrainerId());
            System.out.printf("  %-6s %-18s %-12s %-10s %-11s %-13s %-6d%n",
                    m.getId(), m.getName(), m.getPhone(), plan, m.getJoinDate(),
                    trainer.length() > 13 ? trainer.substring(0, 13) : trainer,
                    service.attendanceCount(m.getId()));
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int askInt(String prompt) {
        String text = ask(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a whole number.");
        }
    }

    private double askDouble(String prompt) {
        String text = ask(prompt);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number.");
        }
    }
}
