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

import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx.ModuleResource;
import de.amr.games.pacman.ui.fx.util.EntryPicker;

/**
 * @author Armin Reichert
 */
public class Talk {

	private Talk() {
	}

	public static final ResourceBundle MESSAGES = ResourceBundle.getBundle(ModuleResource.path("texts/messages"));

	public static String message(String pattern, Object... args) {
		return MESSAGES.getString(pattern).formatted(args);
	}

	public static final EntryPicker<String> CHEAT_TALK = createEntryPicker("texts/cheating_talk");
	public static final EntryPicker<String> LEVEL_COMPLETE_TALK = createEntryPicker("texts/level_complete_talk");
	public static final EntryPicker<String> GAME_OVER_TALK = createEntryPicker("texts/game_over_talk");

	private static EntryPicker<String> createEntryPicker(String relPathToBundle) {
		var bundlePath = ModuleResource.path(relPathToBundle);
		var bundle = ResourceBundle.getBundle(bundlePath);
		return new EntryPicker<>(bundle.keySet().stream().sorted().map(bundle::getString).toArray(String[]::new));
	}
}