package com.gym.model;

/** Anything stored in a {@link com.gym.repo.Repository} must expose a unique id. */
public interface Identifiable {
    String getId();
}
