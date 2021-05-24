package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.Random;

/**
 * Selects repetition-free, random entries from a list.
 * 
 * @author Armin Reichert
 */
public class RandomEntrySelector<T> {
	private T[] entries;
	private int selection;

	@SuppressWarnings("unchecked")
	public RandomEntrySelector(T... entries) {
		this.entries = entries;
		selection = new Random().nextInt(entries.length);
	}

	public T next() {
		if (entries.length == 1) {
			return entries[0];
		}
		int lastSelection = selection;
		while (selection == lastSelection) {
			selection = new Random().nextInt(entries.length);
		}
		return entries[selection];
	}
}