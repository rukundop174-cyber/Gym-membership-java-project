package com.gym.model;

import java.time.LocalDate;
import java.util.Objects;

/** A fee payment made by a member. */
public class Payment implements Identifiable {

    private String id;
    private String memberId;
    private double amount;
    private LocalDate paidOn;
    private PaymentStatus status;

    public Payment() {
    }

    public Payment(String id, String memberId, double amount, LocalDate paidOn, PaymentStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.paidOn = paidOn;
        this.status = status;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getPaidOn() { return paidOn; }
    public void setPaidOn(LocalDate paidOn) { this.paidOn = paidOn; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Payment) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Payment[id=%s, member=%s, amount=%.2f, paidOn=%s, status=%s]",
                id, memberId, amount, paidOn, status);
    }
}
