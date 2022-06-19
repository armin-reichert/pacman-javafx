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

	//@formatter:off
	private static final String OPT_2D               = "-2D";
	private static final String OPT_3D               = "-3D";
	private static final String OPT_FULLSCREEN       = "-fullscreen";
	private static final String OPT_MUTED            = "-muted";
	private static final String OPT_PERSPECTIVE      = "-perspective";
	private static final String OPT_VARIANT_MSPACMAN = "-mspacman";
	private static final String OPT_VARIANT_PACMAN   = "-pacman";
	private static final String OPT_ZOOM             = "-zoom";
	//@formatter:on

	private static final List<String> ALL_OPTIONS = List.of(OPT_2D, OPT_3D, OPT_FULLSCREEN, OPT_MUTED, OPT_PERSPECTIVE,
			OPT_VARIANT_MSPACMAN, OPT_VARIANT_PACMAN, OPT_ZOOM);

	public boolean use3D = true;
	public double zoom = 2.0;
	public boolean fullscreen = false;
	public boolean muted = false;
	public GameVariant gameVariant = GameVariant.PACMAN;
	public Perspective perspective = Perspective.CAM_NEAR_PLAYER;

	private int i;

	public Options(List<String> args) {
		i = 0;
		while (i < args.size()) {
			option0(args, OPT_2D).ifPresent(value -> use3D = false);
			option0(args, OPT_3D).ifPresent(value -> use3D = true);
			option0(args, OPT_FULLSCREEN).ifPresent(value -> fullscreen = value);
			option0(args, OPT_MUTED).ifPresent(value -> muted = value);
			option0(args, OPT_VARIANT_MSPACMAN, Options::convertGameVariant).ifPresent(value -> gameVariant = value);
			option0(args, OPT_VARIANT_PACMAN, Options::convertGameVariant).ifPresent(value -> gameVariant = value);
			option1(args, OPT_PERSPECTIVE, Perspective::valueOf).ifPresent(value -> perspective = value);
			option1(args, OPT_ZOOM, Double::valueOf).ifPresent(value -> zoom = value);
			++i;
		}
	}

	private <T> Optional<T> option1(List<String> args, String name, Function<String, T> fnConvert) {
		if (name.equals(args.get(i))) {
			if (i + 1 == args.size() || ALL_OPTIONS.contains(args.get(i + 1))) {
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

	private <T> Optional<T> option0(List<String> args, String name, Function<String, T> fnConvert) {
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

	private Optional<Boolean> option0(List<String> args, String name) {
		if (name.equals(args.get(i))) {
			log("Found parameter %s", name);
			return Optional.of(true);
		}
		return Optional.empty();
	}

	private static GameVariant convertGameVariant(String s) {
		return switch (s) {
		case OPT_VARIANT_MSPACMAN -> GameVariant.MS_PACMAN;
		case OPT_VARIANT_PACMAN -> GameVariant.PACMAN;
		default -> null;
		};
	}
}