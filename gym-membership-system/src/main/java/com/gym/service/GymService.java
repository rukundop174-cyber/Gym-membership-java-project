package com.gym.service;

import com.gym.model.Attendance;
import com.gym.model.Member;
import com.gym.model.MembershipPlan;
import com.gym.model.Payment;
import com.gym.model.PaymentStatus;
import com.gym.model.Trainer;
import com.gym.repo.Repository;
import com.gym.storage.GymData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Business logic for the gym. Uses generic repositories (HashMap-backed) for storage.
 * Thread-safe: id counters are atomic and repositories are synchronized,
 * so it can be called from the concurrent processor.
 */
public class GymService {

    private final Repository<MembershipPlan> plans = new Repository<>();
    private final Repository<Member> members = new Repository<>();
    private final Repository<Trainer> trainers = new Repository<>();
    private final Repository<Attendance> attendance = new Repository<>();
    private final Repository<Payment> payments = new Repository<>();

    private final AtomicInteger planSeq = new AtomicInteger();
    private final AtomicInteger memberSeq = new AtomicInteger();
    private final AtomicInteger trainerSeq = new AtomicInteger();
    private final AtomicInteger attendanceSeq = new AtomicInteger();
    private final AtomicInteger paymentSeq = new AtomicInteger();

    // ------------------------------------------------------------ plans
    public MembershipPlan addPlan(String name, double monthlyFee, int durationMonths) {
        requireText(name, "Plan name");
        if (monthlyFee <= 0) throw new IllegalArgumentException("Monthly fee must be greater than 0.");
        if (durationMonths <= 0) throw new IllegalArgumentException("Duration must be at least 1 month.");
        MembershipPlan plan = new MembershipPlan(nextId("P", planSeq, 3), name.trim(), monthlyFee, durationMonths);
        plans.add(plan);
        return plan;
    }

    public Optional<MembershipPlan> findPlan(String id) {
        return plans.findById(id);
    }

    public List<MembershipPlan> getAllPlans() {
        return plans.findAll();
    }

    /** Plans sorted by monthly fee using MembershipPlan's Comparable implementation. */
    public List<MembershipPlan> getPlansByFee() {
        return plans.sorted(Comparator.naturalOrder());
    }

    // ---------------------------------------------------------- members
    public Member addMember(String name, String phone, String email, String planId) {
        requireText(name, "Member name");
        if (!plans.contains(planId)) throw new IllegalArgumentException("No plan with id '" + planId + "'.");
        Member member = new Member(nextId("M", memberSeq, 3), name.trim(), phone, email, planId, LocalDate.now());
        members.add(member);
        return member;
    }

    public Optional<Member> findMember(String id) {
        return members.findById(id);
    }

    public List<Member> getAllMembers() {
        return members.findAll();
    }

    /** Case-insensitive search across id, name, phone and email. */
    public List<Member> searchMembers(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return members.search(m -> contains(m.getId(), k) || contains(m.getName(), k)
                || contains(m.getPhone(), k) || contains(m.getEmail(), k));
    }

    public List<Member> getMembersByName() {
        return members.sorted(Comparator.naturalOrder());           // Comparable<Member>
    }

    public List<Member> getMembersByJoinDate() {
        return members.sorted(Comparator.comparing(Member::getJoinDate)   // Comparator
                .thenComparing(Member::getName, String.CASE_INSENSITIVE_ORDER));
    }

    public List<Member> getMembersByPlanFeeDescending() {
        return members.sorted(Comparator.<Member>comparingDouble(this::feeOf).reversed()
                .thenComparing(Comparator.naturalOrder()));
    }

    public boolean removeMember(String id) {
        return members.remove(id);
    }

    public void deactivateMember(String id) {
        requireMember(id).setActive(false);
    }

    // --------------------------------------------------------- trainers
    public Trainer addTrainer(String name, String phone, String email, String specialization) {
        requireText(name, "Trainer name");
        Trainer trainer = new Trainer(nextId("T", trainerSeq, 3), name.trim(), phone, email, specialization);
        trainers.add(trainer);
        return trainer;
    }

    public Optional<Trainer> findTrainer(String id) {
        return trainers.findById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainers.findAll();
    }

    public void assignTrainer(String memberId, String trainerId) {
        Member member = requireMember(memberId);
        if (!trainers.contains(trainerId)) throw new IllegalArgumentException("No trainer with id '" + trainerId + "'.");
        member.setTrainerId(trainerId);
    }

    // ------------------------------------------------------- attendance
    public Attendance recordAttendance(String memberId) {
        Member member = requireMember(memberId);
        if (!member.isActive()) throw new IllegalArgumentException("Member " + memberId + " is inactive.");
        Attendance a = new Attendance(nextId("A", attendanceSeq, 4), memberId, LocalDateTime.now());
        attendance.add(a);
        return a;
    }

    public List<Attendance> getAllAttendance() {
        return attendance.findAll();
    }

    public long attendanceCount(String memberId) {
        return attendance.search(a -> a.getMemberId().equals(memberId)).size();
    }

    // --------------------------------------------------------- payments
    /** Records a payment of the member's monthly plan fee. */
    public Payment recordMonthlyPayment(String memberId) {
        Member member = requireMember(memberId);
        MembershipPlan plan = plans.findById(member.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan missing for member " + memberId));
        return recordPayment(memberId, plan.getMonthlyFee());
    }

    public Payment recordPayment(String memberId, double amount) {
        requireMember(memberId);
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        Payment p = new Payment(nextId("PAY", paymentSeq, 4), memberId, amount, LocalDate.now(), PaymentStatus.PAID);
        payments.add(p);
        return p;
    }

    public List<Payment> getAllPayments() {
        return payments.findAll();
    }

    public List<Payment> getPaymentsByAmountDescending() {
        return payments.sorted(Comparator.comparingDouble(Payment::getAmount).reversed()
                .thenComparing(Payment::getId));
    }

    public double totalRevenue() {
        return payments.search(p -> p.getStatus() == PaymentStatus.PAID)
                .stream().mapToDouble(Payment::getAmount).sum();
    }

    // ------------------------------------------------- save / load glue
    public GymData snapshot() {
        GymData data = new GymData();
        data.setPlans(plans.findAll());
        data.setMembers(members.findAll());
        data.setTrainers(trainers.findAll());
        data.setAttendance(attendance.findAll());
        data.setPayments(payments.findAll());
        return data;
    }

    public void restore(GymData data) {
        plans.replaceAll(orEmpty(data.getPlans()));
        members.replaceAll(orEmpty(data.getMembers()));
        trainers.replaceAll(orEmpty(data.getTrainers()));
        attendance.replaceAll(orEmpty(data.getAttendance()));
        payments.replaceAll(orEmpty(data.getPayments()));

        planSeq.set(maxSuffix(plans.findAll().stream().map(MembershipPlan::getId).toList()));
        memberSeq.set(maxSuffix(members.findAll().stream().map(Member::getId).toList()));
        trainerSeq.set(maxSuffix(trainers.findAll().stream().map(Trainer::getId).toList()));
        attendanceSeq.set(maxSuffix(attendance.findAll().stream().map(Attendance::getId).toList()));
        paymentSeq.set(maxSuffix(payments.findAll().stream().map(Payment::getId).toList()));
    }

    // ----------------------------------------------------------- helpers
    private Member requireMember(String id) {
        return members.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No member with id '" + id + "'."));
    }

    private double feeOf(Member m) {
        return plans.findById(m.getPlanId()).map(MembershipPlan::getMonthlyFee).orElse(0.0);
    }

    private static String nextId(String prefix, AtomicInteger seq, int digits) {
        return prefix + String.format("%0" + digits + "d", seq.incrementAndGet());
    }

    private static int maxSuffix(List<String> ids) {
        int max = 0;
        for (String id : ids) {
            String digits = id.replaceAll("\\D", "");
            if (!digits.isEmpty()) max = Math.max(max, Integer.parseInt(digits));
        }
        return max;
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " must not be empty.");
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
