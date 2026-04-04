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

    private List<String> messages = List.of();
    private int currentIndex;

    public RandomTextPicker(List<String> messages) {
        requireNonNull(messages);
        create(messages);
    }

    public RandomTextPicker(ResourceBundle bundle, String prefix) {
        requireNonNull(bundle);
        requireNonNull(prefix);
        final List<String> messages = bundle.keySet().stream()
            .filter(key -> key.startsWith(prefix))
            .map(bundle::getString)
            .toList();
        create(messages);
    }

    private void create(List<String> messages) {
        if (!messages.isEmpty()) {
            this.messages = new ArrayList<>(messages); // shuffle() needs a mutable list
            Collections.shuffle(this.messages);
        }
        currentIndex = 0;
    }

    public String nextMessage() {
        if (messages.size() == 1) {
            return messages.getFirst();
        }
        String result = messages.get(currentIndex);
        if (++currentIndex == messages.size()) {
            String last = messages.getLast();
            do {
                Collections.shuffle(messages);
            } while (last.equals(messages.getFirst()));
            currentIndex = 0;
        }
        return result;
    }
}