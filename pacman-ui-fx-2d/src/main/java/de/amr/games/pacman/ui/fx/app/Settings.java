/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import java.util.Collections;
import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameVariant;
import javafx.scene.input.KeyCode;

/**
 * @author Armin Reichert
 */
public class Settings {

	private static final Map<Direction, KeyCode> KEYS_NUMPAD = Map.of(//
			Direction.UP, KeyCode.NUMPAD8, //
			Direction.DOWN, KeyCode.NUMPAD5, //
			Direction.LEFT, KeyCode.NUMPAD4, //
			Direction.RIGHT, KeyCode.NUMPAD6);

	private static final Map<Direction, KeyCode> KEYS_CURSOR = Map.of(//
			Direction.UP, KeyCode.UP, //
			Direction.DOWN, KeyCode.DOWN, //
			Direction.LEFT, KeyCode.LEFT, //
			Direction.RIGHT, KeyCode.RIGHT);

	private static Map<Direction, KeyCode> keyMap(String name) {
		return switch (name) {
		case "numpad" -> KEYS_NUMPAD;
		case "cursor" -> KEYS_CURSOR;
		default -> throw new IllegalArgumentException("Unknown keymap name: " + name);
		};
	}

	public boolean fullScreen;
	public GameVariant variant;
	public float zoom;
	public Map<Direction, KeyCode> keyMap;

	public Settings() {
		this(Collections.emptyMap());
	}

	public Settings(Map<String, String> pm) {
		fullScreen = false;
		variant = GameVariant.PACMAN;
		zoom = 2;
		keyMap = keyMap("cursor");
		merge(pm);
	}

	public void merge(Map<String, String> pm) {
		if (pm.containsKey("fullScreen")) {
			fullScreen = Boolean.valueOf(pm.get("fullScreen"));
		}
		if (pm.containsKey("variant")) {
			variant = GameVariant.valueOf(pm.get("variant"));
		}
		if (pm.containsKey("zoom")) {
			zoom = Float.valueOf(pm.get("zoom"));
		}
		if (pm.containsKey("keys")) {
			keyMap = keyMap(pm.get("keys"));
		}
	}

	@Override
	public String toString() {
		return String.format("{fullScreen=%s, variant=%s, zoom=%.2f, keyMap=%s}", fullScreen, variant, zoom, keyMap);
	}
}