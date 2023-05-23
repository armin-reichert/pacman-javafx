/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.rendering2d;

import javafx.scene.paint.Color;

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

	//@formatter:off
	public static final Color PACMAN_MAZE_COLOR_FOOD = Color.rgb(254, 189, 180);
	public static final Color PACMAN_MAZE_COLOR_WALL_TOP = Color.rgb(33, 33, 255).darker();
	public static final Color PACMAN_MAZE_COLOR_WALL_BASE =	Color.rgb(33, 33, 255).brighter();
	public static final Color PACMAN_MAZE_COLOR_DOOR = Color.rgb(252, 181, 255);

	public static final Color[] MS_PACMAN_MAZE_COLORS_FOOD = {
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 255, 0),
		Color.rgb(255,   0, 0),
		Color.rgb(222, 222, 255),
		Color.rgb(0,   255, 255),
		Color.rgb(222, 222, 255),
	};
	
	public static final Color[] MS_PACMAN_MAZE_COLORS_WALL_TOP = {
		Color.rgb(255, 183, 174),  
		Color.rgb( 71, 183, 255), 
		Color.rgb(222, 151,  81), 
		Color.rgb( 33,  33, 255), 
		Color.rgb(255, 183, 255),
		Color.rgb(255, 183, 174), 
	};
	
	public static final Color[] MS_PACMAN_MAZE_COLORS_WALL_BASE = {
		Color.rgb(255,   0,   0),
		Color.rgb(222, 222, 255),
		Color.rgb(222, 222, 255),
		Color.rgb(255, 183,  81),
		Color.rgb(255, 255,   0),
		Color.rgb(255,   0,   0),
	};

	public static final Color MS_PACMAN_MAZE_COLOR_DOOR = Color.rgb(255, 183, 255);
	
	public static final Color PACMAN_COLOR_HEAD              = Color.rgb(255, 255, 0);
	public static final Color PACMAN_COLOR_PALATE            = Color.rgb(191, 79, 61);
	public static final Color PACMAN_COLOR_EYES              = Color.rgb(33, 33, 33);

	public static final Color	MS_PACMAN_COLOR_HEAD           = Color.rgb(255, 255, 0);
	public static final Color	MS_PACMAN_COLOR_PALATE	       = Color.rgb(191, 79, 61);
	public static final Color	MS_PACMAN_COLOR_EYES	         = Color.rgb(33, 33, 33);
	public static final Color	MS_PACMAN_COLOR_HAIRBOW	       = Color.rgb(255, 0, 0);
	public static final Color	MS_PACMAN_COLOR_HAIRBOW_PEARLS = Color.rgb(33, 33, 255);

	public static final Color[][] GHOST_COLORS_NORMAL = { // dress, eyeballs, pupils
		{ RED,    PALE,     BLUE, },
		{ PINK,   PALE,     BLUE, },
		{ CYAN,   PALE,     BLUE, },
		{ ORANGE, PALE,     BLUE, },
	};
	
	public static final Color[] GHOST_COLORS_FRIGHTENED = { BLUE, ROSE,  ROSE }; // dress, eyeballs, pupils
	public static final Color[] GHOST_COLORS_FLASHING = { PALE, ROSE, RED }; // dress, eyeballs,pupils
	//@formatter:on

//	public static final MazeColoring mazeColors(GameVariant variant, int mazeNumber) {
//		return switch (variant) {
//		case MS_PACMAN -> MS_PACMAN_MAZE_COLORS[mazeNumber - 1];
//		case PACMAN -> PACMAN_MAZE_COLORS;
//		default -> throw new IllegalGameVariantException(variant);
//		};
//	}
}