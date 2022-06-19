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
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;

/**
 * Options for the game application.
 * 
 * @author Armin Reichert
 */
class Options {

	/** Command-line argument names. */
	private static final List<String> PARAMETER_NAMES = List.of( //
			"-2D", "-3D", //
			"-zoom", // -zoom <double value>
			"-fullscreen", //
			"-muted", //
			"-mspacman", //
			"-pacman", //
			"-perspective" // see {@link Perspective}
	);

	public boolean use3D = true;
	public double zoom = 2.0;
	public boolean fullscreen = false;
	public boolean muted = false;
	public GameVariant gameVariant = GameVariant.PACMAN;
	public Perspective perspective = Perspective.CAM_NEAR_PLAYER;

	private int i;

	private <T> Optional<T> match1(List<String> args, String name, Function<String, T> fnConvert) {
		if (name.equals(args.get(i))) {
			if (i + 1 == args.size() || PARAMETER_NAMES.contains(args.get(i + 1))) {
				log("!!! Error: missing value for parameter '%s'.", name);
			} else {
				++i;
				try {
					T value = fnConvert.apply(args.get(i));
					log("Found parameter %s = %s", name, value);
					return Optional.ofNullable(value);
				} catch (Exception x) {
					log("!!! Error: '%s' is no legal value for parameter '%s'.", args.get(i), name);
				}
			}
		}
		return Optional.empty();
	}

	private <T> Optional<T> match0(List<String> args, String name, Function<String, T> fnConvert) {
		if (name.equals(args.get(i))) {
			log("Found parameter %s", name);
			try {
				T value = fnConvert.apply(name);
				return Optional.ofNullable(value);
			} catch (Exception x) {
				log("!!! Error: '%s' is no legal parameter.", name);
			}
		}
		return Optional.empty();
	}

	private Optional<Boolean> match0(List<String> args, String name) {
		if (name.equals(args.get(i))) {
			log("Found parameter %s", name);
			return Optional.of(true);
		}
		return Optional.empty();
	}

	private static GameVariant convertGameVariant(String s) {
		return switch (s) {
		case "-mspacman" -> GameVariant.MS_PACMAN;
		case "-pacman" -> GameVariant.PACMAN;
		default -> null;
		};
	}

	public Options(List<String> args) {
		i = 0;
		while (i < args.size()) {
			match1(args, "-zoom", Double::valueOf).ifPresent(value -> zoom = value);
			match0(args, "-fullscreen").ifPresent(value -> fullscreen = value);
			match0(args, "-muted").ifPresent(value -> muted = value);
			match0(args, "-mspacman", Options::convertGameVariant).ifPresent(value -> gameVariant = value);
			match0(args, "-pacman", Options::convertGameVariant).ifPresent(value -> gameVariant = value);
			match0(args, "-2D").ifPresent(value -> use3D = false);
			match0(args, "-3D").ifPresent(value -> use3D = true);
			match1(args, "-perspective", Perspective::valueOf).ifPresent(value -> perspective = value);
			++i;
		}
	}
}