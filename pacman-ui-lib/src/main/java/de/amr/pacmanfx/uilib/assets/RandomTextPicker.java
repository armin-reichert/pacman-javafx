/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Selects entries randomly from a list without repetitions.
 *
 * @author Armin Reichert
 */
public class RandomTextPicker<T> {

    public static RandomTextPicker<String> fromBundle(ResourceBundle bundle, String prefix) {
        requireNonNull(bundle);
        return new RandomTextPicker<>(bundle.keySet().stream()//
            .filter(key -> key.startsWith(prefix))//
            .sorted()//
            .map(bundle::getString)//
            .toArray(String[]::new));
    }

    private List<T> entries = List.of();
    private int current;

    @SuppressWarnings("unchecked")
    public RandomTextPicker(T... items) {
        if (items.length > 0) {
            entries = Arrays.asList(Arrays.copyOf(items, items.length));
            Collections.shuffle(entries);
        }
        current = 0;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
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