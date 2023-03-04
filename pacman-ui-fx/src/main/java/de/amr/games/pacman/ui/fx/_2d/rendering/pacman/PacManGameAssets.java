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

package de.amr.games.pacman.ui.fx._2d.rendering.pacman;

import java.util.Map;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public final class PacManGameAssets {

	static final Spritesheet SPRITESHEET = new Spritesheet(//
			ResourceMgr.image("graphics/pacman/sprites.png"), 16, //
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	static final MazeColoring MAZE_COLORS = new MazeColoring(Color.rgb(254, 189, 180), Color.rgb(33, 33, 255),
			Color.rgb(33, 33, 255), Color.rgb(252, 181, 255));

	static final Image MAZE_FULL = ResourceMgr.image("graphics/pacman/maze_full.png");
	static final Image MAZE_EMPTY = ResourceMgr.image("graphics/pacman/maze_empty.png");
	static final Image MAZE_EMPTY_INV = Ufx.colorsExchanged(MAZE_EMPTY, Map.of(MAZE_COLORS.topColor(), Color.WHITE));
}