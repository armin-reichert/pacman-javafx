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
public class RandomTextPicker {

    private List<String> entries = List.of();
    private int current;

    public RandomTextPicker(List<String> messages) {
        requireNonNull(messages);
        createFromMessages(messages);
    }

    public RandomTextPicker(ResourceBundle bundle, String prefix) {
        requireNonNull(bundle);
        requireNonNull(prefix);
        final List<String> messages = bundle.keySet().stream()
            .filter(key -> key.startsWith(prefix))
            .map(bundle::getString)
            .toList();
        createFromMessages(messages);
    }

    private void createFromMessages(List<String> messages) {
        if (!messages.isEmpty()) {
            this.entries = new ArrayList<>(messages); // shuffle() needs a mutable list
            Collections.shuffle(this.entries);
        }
        current = 0;
    }

    public String nextText() {
        if (entries.size() == 1) {
            return entries.getFirst();
        }
        String result = entries.get(current);
        if (++current == entries.size()) {
            String last = entries.getLast();
            do {
                Collections.shuffle(entries);
            } while (last.equals(entries.getFirst()));
            current = 0;
        }
        return result;
    }
}