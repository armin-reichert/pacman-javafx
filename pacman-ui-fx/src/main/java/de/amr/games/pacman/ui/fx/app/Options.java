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

import de.amr.games.pacman.lib.OptionParser;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;

/**
 * Options for the game application.
 * 
 * @author Armin Reichert
 */
class Options extends OptionParser {

	//@formatter:off
	private static final String OPT_2D          = "-2D";
	private static final String OPT_3D          = "-3D";
	private static final String OPT_FULLSCREEN  = "-fullscreen";
	private static final String OPT_MUTED       = "-muted";
	private static final String OPT_PERSPECTIVE = "-perspective";
	private static final String OPT_ZOOM        = "-zoom";
	//@formatter:on

	public boolean use3D = true;
	public double zoom = 2.0;
	public boolean fullscreen = false;
	public boolean muted = false;
	public GameVariant gameVariant = GameVariant.PACMAN;
	public Perspective perspective = Perspective.NEAR_PLAYER;

	public Options(List<String> args) {
		super(List.of(OPT_2D, OPT_3D, OPT_FULLSCREEN, OPT_MUTED, OPT_PERSPECTIVE, OPT_VARIANT_MSPACMAN, OPT_VARIANT_PACMAN,
				OPT_ZOOM), args);
		while (hasMoreArgs()) {
			use3D = parseBoolean(OPT_2D).orElse(use3D);
			use3D = parseBoolean(OPT_3D).orElse(use3D);
			fullscreen = parseBoolean(OPT_FULLSCREEN).orElse(fullscreen);
			muted = parseBoolean(OPT_MUTED).orElse(muted);
			gameVariant = parseName(OPT_VARIANT_MSPACMAN, OptionParser::convertGameVariant).orElse(gameVariant);
			gameVariant = parseName(OPT_VARIANT_PACMAN, OptionParser::convertGameVariant).orElse(gameVariant);
			perspective = parseNameValue(OPT_PERSPECTIVE, Perspective::valueOf).orElse(perspective);
			zoom = parseNameValue(OPT_ZOOM, Double::valueOf).orElse(zoom);
		}
	}
}