package com.gym.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** One gym check-in by a member. */
public class Attendance implements Identifiable {

    private String id;
    private String memberId;
    private LocalDateTime checkIn;

    public Attendance() {
    }

    public Attendance(String id, String memberId, LocalDateTime checkIn) {
        this.id = id;
        this.memberId = memberId;
        this.checkIn = checkIn;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Attendance) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attendance[id=" + id + ", member=" + memberId + ", checkIn=" + checkIn + "]";
    }
}
