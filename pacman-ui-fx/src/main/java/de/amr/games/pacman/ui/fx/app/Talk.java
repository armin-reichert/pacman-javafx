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

package de.amr.games.pacman.ui.fx.app;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx.util.RandomEntrySelector;

/**
 * @author Armin Reichert
 *
 */
public class Talk {

	private Talk() {
	}

	public static final ResourceBundle MESSAGES = ResourceBundle.getBundle("/common/texts/messages");

	public static String message(String pattern, Object... args) {
		return MessageFormat.format(MESSAGES.getString(pattern), args);
	}

	public static final RandomEntrySelector<String> CHEAT_TALK = load("/common/texts/cheating_talk");
	public static final RandomEntrySelector<String> LEVEL_COMPLETE_TALK = load("/common/texts/level_complete_talk");
	public static final RandomEntrySelector<String> GAME_OVER_TALK = load("/common/texts/game_over_talk");

	private static RandomEntrySelector<String> load(String bundleName) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
		return new RandomEntrySelector<>(bundle.keySet().stream().sorted().map(bundle::getString).toArray(String[]::new));
	}
}
