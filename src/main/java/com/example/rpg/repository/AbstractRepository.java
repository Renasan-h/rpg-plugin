package com.example.rpg.repository;

import org.apache.maven.model.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Repositoryの基幹クラス
 *
 * @param <T> Type
 */
public abstract class AbstractRepository<T> extends Repository {

    private final Map<String, T> data = new HashMap<>();

    public void put(String id, T value) {
        data.put(id, value);
    }

    public T findById(String id) {
        return data.get(id);
    }

    public Collection<T> findAll() {
        return data.values();
    }

    public Map<String, T> findAllAsMap() {
        return Collections.unmodifiableMap(data);
    }

    public boolean exists(String id) {
        return data.containsKey(id);
    }

    public void remove(String id) {
        data.remove(id);
    }

    public void clear() {
        data.clear();
    }

    public int size() {
        return data.size();
    }

    protected Map<String, T> getData() {
        return data;
    }
}
