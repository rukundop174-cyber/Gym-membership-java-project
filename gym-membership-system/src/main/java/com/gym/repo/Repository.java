package com.gym.repo;

import com.gym.model.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic in-memory store backed by a HashMap keyed by id.
 * Methods are synchronized so the repository is safe to use from the
 * ExecutorService worker threads.
 */
public class Repository<T extends Identifiable> {

    private final Map<String, T> items = new HashMap<>();

    public synchronized void add(T item) {
        items.put(item.getId(), item);
    }

    public synchronized Optional<T> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public synchronized boolean contains(String id) {
        return items.containsKey(id);
    }

    public synchronized boolean remove(String id) {
        return items.remove(id) != null;
    }

    public synchronized int size() {
        return items.size();
    }

    /** All items, ordered by id so output is stable. */
    public synchronized List<T> findAll() {
        List<T> list = new ArrayList<>(items.values());
        list.sort(Comparator.comparing(Identifiable::getId));
        return list;
    }

    /** Search with any condition, e.g. repo.search(m -> m.getName().contains("a")). */
    public synchronized List<T> search(Predicate<? super T> condition) {
        return items.values().stream()
                .filter(condition)
                .sorted(Comparator.comparing(Identifiable::getId))
                .collect(Collectors.toList());
    }

    /** Sort with any Comparator (or Comparable's natural order via Comparator.naturalOrder()). */
    public synchronized List<T> sorted(Comparator<? super T> comparator) {
        List<T> list = new ArrayList<>(items.values());
        list.sort(comparator);
        return list;
    }

    /** Replace all contents (used after loading from JSON). */
    public synchronized void replaceAll(Collection<? extends T> newItems) {
        items.clear();
        for (T item : newItems) {
            items.put(item.getId(), item);
        }
    }
}
