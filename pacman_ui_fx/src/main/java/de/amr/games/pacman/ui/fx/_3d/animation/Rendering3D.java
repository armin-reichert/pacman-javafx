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

package de.amr.games.pacman.ui.fx._3d.animation;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class Rendering3D {

	private Rendering3D() {
	}

	//@formatter:off
	private static final Color[] MAZE_TOP_COLORS = { 
		Color.rgb(255, 183, 174), 
		Color.rgb(71, 183, 255), 
		Color.rgb(222, 151, 81), 
		Color.rgb(33, 33, 255), 
		Color.rgb(255, 183, 255), 
		Color.rgb(255, 183, 174), 
	};

	private static final Color[] MAZE_SIDE_COLORS = { 
		Color.rgb(255, 0, 0), 
		Color.rgb(222, 222, 255), 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 183, 81), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0), 
	};
	
	private static final Color[] FOOD_COLORS = {
		Color.rgb(222, 222, 255),
		Color.rgb(255, 255, 0),
		Color.rgb(255, 0, 0),
		Color.rgb(222, 222, 255),
		Color.rgb(0, 255, 255),
		Color.rgb(222, 222, 255),
	};
	//@formatter:on

	public static final GhostColors createGhostColors(int ghostID) {
		var dressColor = switch (ghostID) {
		case Ghost.RED_GHOST -> Color.RED;
		case Ghost.PINK_GHOST -> Color.rgb(252, 181, 255);
		case Ghost.CYAN_GHOST -> Color.CYAN;
		case Ghost.ORANGE_GHOST -> Color.rgb(253, 192, 90);
		default -> Color.WHITE; // should not happen
		};
		return new GhostColors(//
				// dress, eyeballs, pupils
				dressColor, Color.GHOSTWHITE, Color.rgb(33, 33, 255), //
				// in frightened mode
				Color.rgb(33, 33, 255), Color.rgb(245, 189, 180), Color.rgb(252, 187, 179), //
				// when flashing
				Color.rgb(224, 221, 255), Color.rgb(245, 189, 180), Color.RED);
	}

//	public static final Color GHOST_EYE_BALL_COLOR = Color.GHOSTWHITE;
//	public static final Color GHOST_PUPIL_COLOR = Color.rgb(33, 33, 255);
//	public static final Color GHOST_FRIGHTENED_COLOR = Color.rgb(33, 33, 255);
//	public static final Color GHOST_FRIGHTENED_EYE_BALL_COLOR = Color.rgb(245, 189, 180);
//	public static final Color GHOST_FRIGHTENED_PUPIL_COLOR = Color.rgb(252, 187, 179);
//	public static final Color GHOST_FLASHING_COLOR = Color.rgb(224, 221, 255);
//	public static final Color GHOST_FLASHING_PUPIL_COLOR = Color.RED;

	/**
	 * @param gameVariant game variant
	 * @param mazeNumber  the 1-based maze number
	 * @return color of pellets in this maze
	 */
	public static Color getMazeFoodColor(GameVariant gameVariant, int mazeNumber) {
		return switch (gameVariant) {
		case MS_PACMAN -> FOOD_COLORS[mazeNumber - 1];
		case PACMAN -> Color.rgb(254, 189, 180);
		};
	}

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on top (3D) or inside (2D)
	 */
	public static Color getMazeTopColor(GameVariant gameVariant, int mazeNumber) {
		return switch (gameVariant) {
		case MS_PACMAN -> MAZE_TOP_COLORS[mazeNumber - 1];
		case PACMAN -> Color.AZURE;
		};
	}

	/**
	 * @param gameVariant game variant
	 * @param mazeNumber  the 1-based maze number
	 * @return color of maze walls on side (3D) or outside (2D)
	 */
	public static Color getMazeSideColor(GameVariant gameVariant, int mazeNumber) {
		return switch (gameVariant) {
		case MS_PACMAN -> MAZE_SIDE_COLORS[mazeNumber - 1];
		case PACMAN -> Color.rgb(33, 33, 255);
		};
	}

	/**
	 * @param gameVariant game variant
	 * @return color of ghosthouse doors in this maze
	 */
	public static Color getGhostHouseDoorColor(GameVariant gameVariant) {
		return switch (gameVariant) {
		case MS_PACMAN -> Color.rgb(255, 183, 255);
		case PACMAN -> Color.rgb(252, 181, 255);
		};
	}

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	public static Color getGhostDressColor(int ghostID) {
		return switch (ghostID) {
		case Ghost.RED_GHOST -> Color.RED;
		case Ghost.PINK_GHOST -> Color.rgb(252, 181, 255);
		case Ghost.CYAN_GHOST -> Color.CYAN;
		case Ghost.ORANGE_GHOST -> Color.rgb(253, 192, 90);
		default -> Color.WHITE; // should not happen
		};
	}
}