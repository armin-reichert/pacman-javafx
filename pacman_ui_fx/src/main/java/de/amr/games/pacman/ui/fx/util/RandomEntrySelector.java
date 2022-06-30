/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.util;

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