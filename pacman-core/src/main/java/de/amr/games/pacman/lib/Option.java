/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.util.function.Function;

/**
 * @author Armin Reichert
 */
public class Option<T> {

	public static Option<Boolean> booleanOption(String name, boolean defaultValue) {
		return new Option<>(name, defaultValue, Boolean::valueOf);
	}

	public static Option<Integer> integerOption(String name, int defaultValue) {
		return new Option<>(name, defaultValue, Integer::valueOf);
	}

	public static Option<Float> floatOption(String name, float defaultValue) {
		return new Option<>(name, defaultValue, Float::valueOf);
	}

	public static Option<Double> doubleOption(String name, double defaultValue) {
		return new Option<>(name, defaultValue, Double::valueOf);
	}

	public static <X> Option<X> option(String name, X defaultValue, Function<String, X> fnValueOf) {
		return new Option<>(name, defaultValue, fnValueOf);
	}

	private final String name;
	private final Function<String, T> fnValueOf;
	private T value;

	private Option(String name, T defaultValue, Function<String, T> fnValueOf) {
		this.name = name;
		this.value = defaultValue;
		this.fnValueOf = fnValueOf;
	}

	@Override
	public String toString() {
		return String.format("[Option name=%s value=%s]", name, value);
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public void parse(String s) {
		try {
			value = fnValueOf.apply(s);
			Logger.info("Found option: {} = {}", name, value);
		} catch (Exception e) {
			Logger.error("Could not parse option '{}' from text '{}'", name, s);
		}
	}
}