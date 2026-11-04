package com.gym.storage;

import com.gym.model.Member;
import com.gym.model.MembershipPlan;
import com.gym.model.Payment;
import com.gym.service.GymService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Plain-text report written with java.nio file I/O. */
public class ReportWriter {

    public void write(GymService service, Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("GYM MEMBERSHIP REPORT");
            out.newLine();
            out.write("=====================");
            out.newLine();
            out.newLine();

            out.write("Plans:");
            out.newLine();
            for (MembershipPlan p : service.getPlansByFee()) {
                out.write("  " + p);
                out.newLine();
            }

            out.newLine();
            out.write("Members (A-Z):");
            out.newLine();
            for (Member m : service.getMembersByName()) {
                out.write(String.format("  %s  %-20s check-ins: %d",
                        m.getId(), m.getName(), service.attendanceCount(m.getId())));
                out.newLine();
            }

            out.newLine();
            out.write("Payments:");
            out.newLine();
            for (Payment p : service.getAllPayments()) {
                out.write("  " + p);
                out.newLine();
            }
            out.newLine();
            out.write(String.format("Total revenue: %.2f", service.totalRevenue()));
            out.newLine();
        }
    }
}
