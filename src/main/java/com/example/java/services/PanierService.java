package com.example.java.services;

import com.example.java.entities.Oeuvre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanierService {
    private static final PanierService INSTANCE = new PanierService();

    private final List<Oeuvre> items = new ArrayList<>();

    private PanierService() {
    }

    public static PanierService getInstance() {
        return INSTANCE;
    }

    public synchronized boolean add(Oeuvre oeuvre) {
        if (oeuvre == null || contains(oeuvre.getId())) {
            return false;
        }
        items.add(oeuvre);
        return true;
    }

    public synchronized void remove(int oeuvreId) {
        items.removeIf(oeuvre -> oeuvre.getId() == oeuvreId);
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized List<Oeuvre> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized double total() {
        return items.stream().mapToDouble(Oeuvre::getPrix).sum();
    }

    public synchronized boolean contains(int oeuvreId) {
        return items.stream().anyMatch(oeuvre -> oeuvre.getId() == oeuvreId);
    }
}
