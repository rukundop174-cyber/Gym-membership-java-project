package com.gym.concurrent;

import java.util.List;

/** Outcome of a concurrent batch: what succeeded and what failed (with reasons). */
public record BatchResult<T>(List<T> succeeded, List<String> failures) {
}
