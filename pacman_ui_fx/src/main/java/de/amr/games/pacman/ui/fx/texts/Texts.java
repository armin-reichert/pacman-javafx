/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx.texts;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.util.EntryPicker;

/**
 * @author Armin Reichert
 */
public class Texts {

	private Texts() {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");

	public static String message(String pattern, Object... args) {
		try {
			return BUNDLE.getString(pattern).formatted(args);
		} catch (MissingResourceException x) {
			LOGGER.error("No text resource found for key '%s'", pattern);
			return "{%s}".formatted(pattern);
		}
	}

	public static final EntryPicker<String> TALK_CHEATING = createEntryPicker("cheating");
	public static final EntryPicker<String> TALK_LEVEL_COMPLETE = createEntryPicker("level.complete");
	public static final EntryPicker<String> TALK_GAME_OVER = createEntryPicker("game.over");

	private static EntryPicker<String> createEntryPicker(String prefix) {
		return new EntryPicker<>(BUNDLE.keySet().stream()//
				.filter(key -> key.startsWith(prefix))//
				.sorted()//
				.map(BUNDLE::getString)//
				.toArray(String[]::new));
	}
}