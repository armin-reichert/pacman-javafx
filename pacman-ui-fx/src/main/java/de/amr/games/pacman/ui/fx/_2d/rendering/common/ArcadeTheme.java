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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ArcadeTheme {

	public class Palette {
		public static final Color RED = Color.rgb(255, 0, 0);
		public static final Color YELLOW = Color.YELLOW;
		public static final Color PINK = Color.rgb(252, 181, 255);
		public static final Color CYAN = Color.rgb(0, 255, 255);
		public static final Color ORANGE = Color.rgb(251, 190, 88);
		public static final Color BLUE = Color.rgb(33, 33, 255);
		public static final Color PALE = Color.rgb(222, 222, 255);
		public static final Color ROSE = Color.rgb(252, 187, 179);
	}

	public static final GhostColoring[] GHOST_COLORS = new GhostColoring[4];

	static {
		//@formatter:off
		GHOST_COLORS[Ghost.ID_RED_GHOST] = new GhostColoring(
			Palette.RED,  Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_PINK_GHOST] = new GhostColoring(
			Palette.PINK, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_CYAN_GHOST] = new GhostColoring(
			Palette.CYAN, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);
		
		GHOST_COLORS[Ghost.ID_ORANGE_GHOST] = new GhostColoring(
			Palette.ORANGE, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE,   Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE,   Palette.ROSE, Palette.RED   // flashing
		);
		//@formatter:on
	}

	public static final Font SCREEN_FONT = ResourceMgr.font("fonts/emulogic.ttf", TS);
}