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

import java.util.List;

import de.amr.games.pacman.lib.Option;
import de.amr.games.pacman.lib.OptionParser;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;

/**
 * Options for the game application.
 * 
 * @author Armin Reichert
 */
class Options {

	private Options() {
	}

	static final Option<Boolean> OPT_3D = new Option<>("-3D", false, Boolean::valueOf);
	static final Option<Boolean> OPT_FULLSCREEN = new Option<>("-fullscreen", false, Boolean::valueOf);
	static final Option<Boolean> OPT_MUTED = new Option<>("-muted", false, Boolean::valueOf);
	static final Option<Perspective> OPT_PERSPECTIVE = new Option<>("-perspective", Perspective.NEAR_PLAYER,
			Perspective::valueOf);
	static final Option<GameVariant> OPT_VARIANT = new Option<>("-variant", GameVariant.PACMAN, GameVariant::valueOf);
	static final Option<Double> OPT_ZOOM = new Option<>("-zoom", 2.0, Double::valueOf);

	public static void parse(List<String> args) {
		var parser = new OptionParser(List.of(OPT_3D, OPT_FULLSCREEN, OPT_MUTED, OPT_PERSPECTIVE, OPT_VARIANT, OPT_ZOOM),
				args);
		while (parser.hasMoreArgs()) {
			parser.parseValue(OPT_3D);
			parser.parseValue(OPT_FULLSCREEN);
			parser.parseValue(OPT_MUTED);
			parser.parseValue(OPT_VARIANT);
			parser.parseValue(OPT_PERSPECTIVE);
			parser.parseValue(OPT_ZOOM);
		}
	}
}