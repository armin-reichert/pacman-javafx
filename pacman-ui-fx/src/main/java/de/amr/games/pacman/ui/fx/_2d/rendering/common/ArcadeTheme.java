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

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ArcadeTheme {

	public static final Color RED = Color.rgb(255, 0, 0);
	public static final Color YELLOW = Color.rgb(255, 255, 0);
	public static final Color PINK = Color.rgb(252, 181, 255);
	public static final Color CYAN = Color.rgb(0, 255, 255);
	public static final Color ORANGE = Color.rgb(251, 190, 88);
	public static final Color BLACK = Color.rgb(0, 0, 0);
	public static final Color BLUE = Color.rgb(33, 33, 255);
	public static final Color PALE = Color.rgb(222, 222, 255);
	public static final Color ROSE = Color.rgb(252, 187, 179);

	public static final GhostColoring[] GHOST_COLORS = new GhostColoring[4];

	static {
		//@formatter:off
		GHOST_COLORS[Ghost.ID_RED_GHOST] = new GhostColoring(
			RED,  PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_PINK_GHOST] = new GhostColoring(
			PINK, PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_CYAN_GHOST] = new GhostColoring(
			CYAN, PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);
		
		GHOST_COLORS[Ghost.ID_ORANGE_GHOST] = new GhostColoring(
			ORANGE, PALE, BLUE, // normal
			BLUE,   ROSE, ROSE, // frightened
			PALE,   ROSE, RED   // flashing
		);
		//@formatter:on
	}

	public static final Font SCREEN_FONT = ResourceMgr.font("fonts/emulogic.ttf", 8);
}