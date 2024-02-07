/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 *
 * @param <E> enum type which should get these methods
 */
public interface EnumMethods<E extends Enum<E>> {

	@SuppressWarnings("unchecked")
	default Class<E> getEnumClass() {
		return (Class<E>) getClass();
	}

	int ordinal();

	default E[] enumValues() {
		return getEnumClass().getEnumConstants();
	}

	default E prev() {
		E[] values = enumValues();
		return values[ordinal() == 0 ? values.length - 1 : ordinal() - 1];
	}

	default E next() {
		E[] values = enumValues();
		return values[ordinal() == values.length - 1 ? 0 : ordinal() + 1];
	}
}