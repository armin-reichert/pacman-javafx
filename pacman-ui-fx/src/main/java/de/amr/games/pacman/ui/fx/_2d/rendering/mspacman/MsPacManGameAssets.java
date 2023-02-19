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

package de.amr.games.pacman.ui.fx._2d.rendering.mspacman;

import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
final class MsPacManGameAssets {

	//@formatter:off
	public static final Color[] MAZE_TOP_COLORS = { 
		Color.rgb(255, 183, 174), 
		Color.rgb(71, 183, 255), 
		Color.rgb(222, 151, 81), 
		Color.rgb(33, 33, 255), 
		Color.rgb(255, 183, 255), 
		Color.rgb(255, 183, 174), 
	};

	public static final Color[] MAZE_SIDE_COLORS = { 
		Color.rgb(255, 0, 0), 
		Color.rgb(222, 222, 255), 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 183, 81), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0), 
	};

	public static final Color[] FOOD_COLORS = { 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0),
		Color.rgb(222, 222, 255), 
		Color.rgb(0, 255, 255), 
		Color.rgb(222, 222, 255), 
	};
	//@formatter:on

	public static final Color GHOSTHOUSE_DOOR_COLOR = Color.rgb(255, 183, 255);

	public static final int MAZE_WIDTH = 226;
	public static final int MAZE_HEIGHT = 248;

	public static final int SECOND_COLUMN = 228;
	public static final int THIRD_COLUMN = 456;

	public static final Spritesheet SPRITESHEET = new Spritesheet(//
			ResourceMgr.image("graphics/mspacman/sprites.png"), 16, //
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	public static final Image MIDWAY_LOGO = ResourceMgr.image("graphics/mspacman/midway.png");

	public static final Image[] MAZES_EMPTY_INV = new Image[6];

	static {
		for (int i = 0; i < MAZES_EMPTY_INV.length; ++i) {
			var maze = SPRITESHEET.subImage(SECOND_COLUMN, MAZE_HEIGHT * i, MAZE_WIDTH, MAZE_HEIGHT);
			MAZES_EMPTY_INV[i] = Ufx.colorsExchanged(maze, Map.of(//
					MsPacManGameAssets.MAZE_SIDE_COLORS[i], Color.WHITE, //
					MsPacManGameAssets.MAZE_TOP_COLORS[i], Color.BLACK));
		}
	}

	// tile from third column
	public static Rectangle2D col3(int col, int row) {
		return SPRITESHEET.region(THIRD_COLUMN, 0, col, row, 1, 1);
	}

	private MsPacManGameAssets() {
	}
}