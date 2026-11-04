package com.gym.storage;

import com.gym.model.Attendance;
import com.gym.model.Member;
import com.gym.model.MembershipPlan;
import com.gym.model.Payment;
import com.gym.model.Trainer;

import java.util.ArrayList;
import java.util.List;

/** Plain container that is written to / read from the JSON file. */
public class GymData {

    private List<MembershipPlan> plans = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private List<Trainer> trainers = new ArrayList<>();
    private List<Attendance> attendance = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();

    public List<MembershipPlan> getPlans() { return plans; }
    public void setPlans(List<MembershipPlan> plans) { this.plans = plans; }

    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> members) { this.members = members; }

    public List<Trainer> getTrainers() { return trainers; }
    public void setTrainers(List<Trainer> trainers) { this.trainers = trainers; }

    public List<Attendance> getAttendance() { return attendance; }
    public void setAttendance(List<Attendance> attendance) { this.attendance = attendance; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}
