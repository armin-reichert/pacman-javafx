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

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;

/**
 * Options for the game application.
 * 
 * @author Armin Reichert
 */
class Options {

	/** Command-line argument names. */
	static final List<String> NAMES = List.of( //
			"-2D", "-3D", //
			"-zoom", // -zoom <double value>
			"-fullscreen", //
			"-muted", //
			"-mspacman", "-pacman", //
			"-perspective" // see {@link Perspective}
	);

	public boolean use3D = true;
	public double zoom = 2.0;
	public boolean fullscreen = false;
	public boolean muted = false;
	public GameVariant gameVariant = GameVariant.PACMAN;
	public Perspective perspective = Perspective.CAM_NEAR_PLAYER;

	public Options(List<String> args) {
		int i = 0;
		while (i < args.size()) {

			// -zoom <double value>
			if ("-zoom".equals(args.get(i))) {
				if (i + 1 == args.size() || NAMES.contains(args.get(i + 1))) {
					log("!!! Error parsing parameters: missing zoom value.");
				} else {
					++i;
					try {
						zoom = Double.parseDouble(args.get(i));
					} catch (NumberFormatException x) {
						log("!!! Error parsing parameters: '%s' is no legal zoom value.", args.get(i));
					}
				}
			}

			// -fullscreen
			else if ("-fullscreen".equals(args.get(i))) {
				fullscreen = true;
			}

			// -muted
			else if ("-muted".equals(args.get(i))) {
				muted = true;
			}

			// -mspacman
			else if ("-mspacman".equals(args.get(i))) {
				gameVariant = GameVariant.MS_PACMAN;
			}

			// -pacman
			else if ("-pacman".equals(args.get(i))) {
				gameVariant = GameVariant.PACMAN;
			}

			// -2D
			else if ("-2D".equals(args.get(i))) {
				use3D = false;
			}

			// -3D
			else if ("-3D".equals(args.get(i))) {
				use3D = true;
			}

			// -perspective
			else if ("-perspective".equals(args.get(i))) {
				if (i + 1 == args.size() || NAMES.contains(args.get(i + 1))) {
					log("!!! Error parsing parameters: missing perspective value.");
				} else {
					++i;
					try {
						perspective = Perspective.valueOf(args.get(i).toUpperCase());
					} catch (Exception x) {
						log("!!! Error parsing parameters: '%s' is no legal perspective value.", args.get(i));
					}
				}
			}

			else {
				log("!!! Error parsing parameters: Found garbage '%s'", args.get(i));
			}

			++i;
		}
	}
}