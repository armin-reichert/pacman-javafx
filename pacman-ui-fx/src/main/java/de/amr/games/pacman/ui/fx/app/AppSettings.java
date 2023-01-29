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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import javafx.application.Application;

/**
 * @author Armin Reichert
 */
class AppSettings {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public final boolean fullScreen;
	public final Perspective perspective;
	public final boolean use3D;
	public final GameVariant variant;
	public final float zoom;

	private final Map<String, String> parameters;

	public AppSettings(Application app) {
		this.parameters = Objects.requireNonNull(app).getParameters().getNamed();
		fullScreen = parseBoolean("fullScreen", false);
		perspective = parse("perspective", Perspective.NEAR_PLAYER, Perspective::valueOf);
		use3D = parseBoolean("use3D", false);
		variant = parse("variant", GameVariant.PACMAN, GameVariant::valueOf);
		zoom = parseFloat("zoom", 2.0);
	}

	@Override
	public String toString() {
		return "{fullScreen=%s, perspective=%s, use3D=%s, variant=%s, zoom=%.2f}".formatted(fullScreen, perspective, use3D,
				variant, zoom);
	}

	private boolean parseBoolean(String key, boolean defaultValue) {
		try {
			return Boolean.valueOf(parameters.getOrDefault(key, String.valueOf(defaultValue)));
		} catch (Exception e) {
			LOG.error("Error parsing boolean parameter '%s': %s", key, e.getMessage());
			return defaultValue;
		}
	}

	private float parseFloat(String key, double defaultValue) {
		try {
			return Float.valueOf(parameters.getOrDefault(key, String.valueOf(defaultValue)));
		} catch (Exception e) {
			LOG.error("Error parsing floating point parameter '%s': %s", key, e.getMessage());
			return (float) defaultValue;
		}
	}

	private <T> T parse(String key, T defaultValue, Function<String, T> parser) {
		try {
			return parser.apply(parameters.getOrDefault(key, String.valueOf(defaultValue)));
		} catch (Exception e) {
			LOG.error("Error parsing parameter '%s': %s", key, e.getMessage());
			return defaultValue;
		}
	}
}