/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Selects entries randomly from a list without repetitions.
 */
public class RandomTextPicker<T> {

    public static RandomTextPicker<String> fromBundle(ResourceBundle bundle, String prefix) {
        requireNonNull(bundle);
        requireNonNull(prefix);
        final List<String> messages = bundle.keySet().stream()
            .filter(key -> key.startsWith(prefix))
            .map(bundle::getString)
            .toList();
        return new RandomTextPicker<>(messages);
    }

    private List<T> entries = List.of();
    private int current;

    public RandomTextPicker(List<T> entries) {
        if (!entries.isEmpty()) {
            this.entries = new ArrayList<>(entries);
            Collections.shuffle(this.entries);
        }
        current = 0;
    }

    public T nextText() {
        if (entries.size() == 1) {
            return entries.getFirst();
        }
        T result = entries.get(current);
        if (++current == entries.size()) {
            T last = entries.getLast();
            do {
                Collections.shuffle(entries);
            } while (last.equals(entries.getFirst()));
            current = 0;
        }
        return result;
    }
}