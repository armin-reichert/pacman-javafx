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

import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import javafx.scene.input.KeyCode;

/**
 * @author Armin Reichert
 */
public class Settings {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static <T> T parse(Map<String, String> parameters, String key, T defaultValue, Function<String, T> parser) {
		try {
			String valueAsString = parameters.getOrDefault(key, String.valueOf(defaultValue));
			return parser.apply(valueAsString);
		} catch (Exception e) {
			LOG.error("Error parsing parameter '%s': %s", key, e.getMessage());
			return defaultValue;
		}
	}

	private static Map<Direction, KeyCode> parseKeyMap(String spec) {
		return switch (spec) {
		case "numpad" -> Map.of(//
				Direction.UP, KeyCode.NUMPAD8, //
				Direction.DOWN, KeyCode.NUMPAD5, //
				Direction.LEFT, KeyCode.NUMPAD4, //
				Direction.RIGHT, KeyCode.NUMPAD6);
		default -> Map.of(//
				Direction.UP, KeyCode.UP, //
				Direction.DOWN, KeyCode.DOWN, //
				Direction.LEFT, KeyCode.LEFT, //
				Direction.RIGHT, KeyCode.RIGHT);
		};
	}

	public final boolean fullScreen;
	public final boolean muted;
	public final Perspective perspective;
	public final boolean use3D;
	public final GameVariant variant;
	public final float zoom;
	public final Map<Direction, KeyCode> keyMap;
	public final boolean useTestRenderer;

	@SuppressWarnings("unchecked")
	public Settings(Map<String, String> parameters) {
		fullScreen = parse(parameters, "fullScreen", false, Boolean::valueOf);
		muted = parse(parameters, "muted", false, Boolean::valueOf);
		perspective = parse(parameters, "perspective", Perspective.NEAR_PLAYER, Perspective::valueOf);
		use3D = parse(parameters, "use3D", false, Boolean::valueOf);
		variant = parse(parameters, "variant", GameVariant.PACMAN, GameVariant::valueOf);
		zoom = parse(parameters, "zoom", 2.0f, Float::valueOf);
		keyMap = (Map<Direction, KeyCode>) parse(parameters, "keys", "cursor", Settings::parseKeyMap);
		useTestRenderer = parse(parameters, "useTestRenderer", false, Boolean::valueOf);
	}

	@Override
	public String toString() {
		return "{fullScreen=%s, muted=%s, perspective=%s, use3D=%s, variant=%s, zoom=%.2f, keys=%s}".formatted(fullScreen,
				muted, perspective, use3D, variant, zoom, keyMap);
	}
}