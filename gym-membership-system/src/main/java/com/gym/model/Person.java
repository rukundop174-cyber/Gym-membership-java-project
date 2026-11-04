package com.gym.model;

import java.util.Objects;

/**
 * Abstract base class for people in the gym (members and trainers).
 * Demonstrates encapsulation, abstract classes, and equals/hashCode/toString.
 */
public abstract class Person implements Identifiable {

    private String id;
    private String name;
    private String phone;
    private String email;

    /** Needed by Jackson for JSON loading. */
    protected Person() {
    }

    protected Person(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /** Each subclass describes its own role (method overriding / polymorphism). */
    public abstract String getRole();

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person other = (Person) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getRole() + "[id=" + id + ", name=" + name + ", phone=" + phone + ", email=" + email + "]";
    }
}
