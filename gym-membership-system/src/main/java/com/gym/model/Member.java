package com.gym.model;

import java.time.LocalDate;

/** A gym member. Extends {@link Person}; sorted by name by default (Comparable). */
public class Member extends Person implements Comparable<Member> {

    private String planId;
    private String trainerId;      // may be null
    private LocalDate joinDate;
    private boolean active = true;

    public Member() {
    }

    public Member(String id, String name, String phone, String email, String planId, LocalDate joinDate) {
        super(id, name, phone, email);
        this.planId = planId;
        this.joinDate = joinDate;
    }

    @Override
    public String getRole() {
        return "Member";
    }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Natural ordering: alphabetical by name (Comparable). */
    @Override
    public int compareTo(Member other) {
        return getName().compareToIgnoreCase(other.getName());
    }

    @Override
    public String toString() {
        return super.toString() + " {plan=" + planId + ", trainer=" + trainerId
                + ", joined=" + joinDate + ", active=" + active + "}";
    }
}
