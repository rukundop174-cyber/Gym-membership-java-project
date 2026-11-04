package com.gym.model;

import java.util.Objects;

/** A membership plan (e.g. Basic / Premium). Comparable by monthly fee. */
public class MembershipPlan implements Identifiable, Comparable<MembershipPlan> {

    private String id;
    private String name;
    private double monthlyFee;
    private int durationMonths;

    public MembershipPlan() {
    }

    public MembershipPlan(String id, String name, double monthlyFee, int durationMonths) {
        this.id = id;
        this.name = name;
        this.monthlyFee = monthlyFee;
        this.durationMonths = durationMonths;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }

    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }

    @Override
    public int compareTo(MembershipPlan other) {
        return Double.compare(monthlyFee, other.monthlyFee);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((MembershipPlan) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("MembershipPlan[id=%s, name=%s, fee=%.2f/month, duration=%d months]",
                id, name, monthlyFee, durationMonths);
    }
}
