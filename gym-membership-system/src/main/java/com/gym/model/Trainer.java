package com.gym.model;

/** A personal trainer. Second subclass of {@link Person}. */
public class Trainer extends Person {

    private String specialization;

    public Trainer() {
    }

    public Trainer(String id, String name, String phone, String email, String specialization) {
        super(id, name, phone, email);
        this.specialization = specialization;
    }

    @Override
    public String getRole() {
        return "Trainer";
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    @Override
    public String toString() {
        return super.toString() + " {specialization=" + specialization + "}";
    }
}
