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
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class DebugDraw {

	public static void drawTileBorders(GraphicsContext g, Stream<V2i> tiles, Color color) {
		tiles.forEach(tile -> drawTileBorder(g, tile, color));
	}

	private static void drawTileBorder(GraphicsContext g, V2i tile, Color color) {
		g.setStroke(color);
		g.strokeRect(t(tile.x) + 0.2, t(tile.y) + 0.2, TS - 0.2, TS - 0.2);
	}

	public static void drawGrid(GraphicsContext g) {
		g.setStroke(Color.rgb(160, 160, 160, 0.5));
		g.setLineWidth(1);
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			line(g, 0, t(row), t(ArcadeWorld.TILES_X), t(row));
		}
		for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
			line(g, t(col), 0, t(col), t(ArcadeWorld.TILES_Y));
		}
	}

	// WTF: why are lines blurred without this?
	private static void line(GraphicsContext g, double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		g.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}

}
