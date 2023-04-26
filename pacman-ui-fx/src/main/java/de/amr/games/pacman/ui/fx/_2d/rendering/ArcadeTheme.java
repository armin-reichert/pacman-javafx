/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.rendering;

import de.amr.games.pacman.model.GameModel;
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

	static final Font SCREEN_FONT = ResourceMgr.font("fonts/emulogic.ttf", 8);

	static final GhostColoring[] GHOST_COLORING = new GhostColoring[4];

	//@formatter:off
	static {
		GHOST_COLORING[GameModel.RED_GHOST] = new GhostColoring(
			RED,  PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);

		GHOST_COLORING[GameModel.PINK_GHOST] = new GhostColoring(
			PINK, PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);

		GHOST_COLORING[GameModel.CYAN_GHOST] = new GhostColoring(
			CYAN, PALE, BLUE, // normal
			BLUE, ROSE, ROSE, // frightened
			PALE, ROSE, RED   // flashing
		);
		
		GHOST_COLORING[GameModel.ORANGE_GHOST] = new GhostColoring(
			ORANGE, PALE, BLUE, // normal
			BLUE,   ROSE, ROSE, // frightened
			PALE,   ROSE, RED   // flashing
		);
	}

	static final MazeColoring PACMAN_MAZE_COLORS = new MazeColoring(//
			Color.rgb(254, 189, 180), // food color
			Color.rgb(33, 33, 255).darker(), // wall top color
			Color.rgb(33, 33, 255).brighter(), // wall side color
			Color.rgb(252, 181, 255) // ghosthouse door color
	);

	static final MazeColoring[] MS_PACMAN_MAZE_COLORS = {
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(255, 255, 0),   Color.rgb( 71, 183, 255),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(255,   0, 0),   Color.rgb(222, 151,  81),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb( 33,  33, 255),  Color.rgb(255, 183,  81), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(0,   255, 255), Color.rgb(255, 183, 255),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
	};

	static final PacManColoring PACMAN_COLORING = new PacManColoring(
		Color.rgb(255, 255, 0), // head
		Color.rgb(191, 79, 61), // palate
		Color.rgb(33, 33, 33)   // eyes
	);

	static final MsPacManColoring MS_PACMAN_COLORING = new MsPacManColoring(
		Color.rgb(255, 255, 0), // head
		Color.rgb(191, 79, 61), // palate
		Color.rgb(33, 33, 33),  // eyes
		Color.rgb(255, 0, 0),   // hair bow
		Color.rgb(33, 33, 255)  // hair bow pearls
	);
	//@formatter:on
}