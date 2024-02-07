/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class OptionParser {

	private final Map<String, Option<?>> optionMap = new HashMap<>();
	private int cursor;

	public OptionParser(Option<?>... options) {
		Arrays.asList(options).forEach(option -> optionMap.put(option.getName(), option));
	}

	public void parse(List<String> arglist) {
		cursor = 0;
		while (cursor < arglist.size()) {
			optionMap.values().forEach(option -> parseValue(option, arglist));
		}
	}

	public void parse(String... args) {
		parse(Arrays.asList(args));
	}

	private <T> void parseValue(Option<T> option, List<String> arglist) {
		if (cursor < arglist.size()) {
			var arg1 = arglist.get(cursor);
			if (!optionMap.keySet().contains(arg1)) {
				Logger.error("Skip garbage '{}'", arg1);
				++cursor;
				return;
			}
			if (option.getName().equals(arg1)) {
				++cursor;
				if (cursor < arglist.size()) {
					var arg2 = arglist.get(cursor);
					if (optionMap.keySet().contains(arg2)) {
						Logger.error("Missing value for parameter '{}'.", option.getName());
					} else {
						++cursor;
						option.parse(arg2);
					}
				}
			}
		}
	}
}