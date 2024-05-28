/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Selects entries randomly from a list without repetitions.
 *
 * @author Armin Reichert
 */
public class Picker<T> {

    public static Picker<String> fromBundle(ResourceBundle bundle, String prefix) {
        checkNotNull(bundle);
        return new Picker<>(bundle.keySet().stream()//
            .filter(key -> key.startsWith(prefix))//
            .sorted()//
            .map(bundle::getString)//
            .toArray(String[]::new));
    }

    private final List<T> entries;
    private int current;

    @SuppressWarnings("unchecked")
    public Picker(T... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Must provide at least one item to select");
        }
        this.entries = Arrays.asList(Arrays.copyOf(items, items.length));
        Collections.shuffle(entries);
        current = 0;
    }

    public T next() {
        if (entries.size() == 1) {
            return entries.get(0);
        }
        T result = entries.get(current);
        if (++current == entries.size()) {
            T last = entries.get(entries.size() - 1);
            do {
                Collections.shuffle(entries);
            } while (last.equals(entries.get(0)));
            current = 0;
        }
        return result;
    }
}