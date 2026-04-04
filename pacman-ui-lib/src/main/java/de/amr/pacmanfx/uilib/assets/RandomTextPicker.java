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

    private List<String> texts = List.of();
    private int currentIndex;

    public RandomTextPicker(List<String> texts) {
        requireNonNull(texts);
        create(texts);
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
            this.texts = new ArrayList<>(messages); // shuffle() needs a mutable list
            Collections.shuffle(this.texts);
        }
        currentIndex = 0;
    }

    public String selectNextText() {
        if (texts.size() == 1) {
            return texts.getFirst();
        }
        String result = texts.get(currentIndex);
        if (++currentIndex == texts.size()) {
            String last = texts.getLast();
            do {
                Collections.shuffle(texts);
            } while (last.equals(texts.getFirst()));
            currentIndex = 0;
        }
        return result;
    }
}