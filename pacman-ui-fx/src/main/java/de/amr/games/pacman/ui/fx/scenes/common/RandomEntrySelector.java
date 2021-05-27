package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Selects entries randomly from a list.
 * 
 * @author Armin Reichert
 */
public class RandomEntrySelector<T> {
	private List<T> entries;
	private int current;

	@SuppressWarnings("unchecked")
	public RandomEntrySelector(T... items) {
		if (items.length == 0) {
			throw new IllegalArgumentException("Must provide at least one item to select");
		}
		this.entries = new ArrayList<>(Arrays.asList(items));
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