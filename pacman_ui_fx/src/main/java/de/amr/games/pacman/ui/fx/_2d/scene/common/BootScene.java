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

package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Random;

import de.amr.games.pacman.model.common.world.ArcadeWorld;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private final Random rnd = new Random();
	private final GraphicsContext buffer;
	private final WritableImage currentImage;

	public BootScene() {
		var width = (int) unscaledSize.x();
		var height = (int) unscaledSize.y();
		buffer = new Canvas(width, height).getGraphicsContext2D();
		currentImage = new WritableImage(width, height);
	}

	@Override
	public void init() {
		clearBuffer();
	}

	@Override
	public void update() {
		var timer = ctx.state().timer();
		if (timer.atSecond(3.5)) {
			ctx.gameController().terminateCurrentState();
		} else {
			if (timer.betweenSeconds(0.5, 1.5) && timer.tick() % 5 == 0) {
				drawRandomHexCodes(buffer);
			} else if (timer.betweenSeconds(1.5, 3.0) && timer.tick() % 10 == 0) {
				drawRandomSprites(buffer);
			} else if (timer.atSecond(3.0)) {
				drawGrid(buffer);
			}
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		g.drawImage(currentImage, 0, 0);
	}

	@Override
	public void drawHUD(GraphicsContext g) {
		// hide HUD
	}

	private void clearBuffer() {
		buffer.setFill(Color.BLACK);
		buffer.fillRect(0, 0, buffer.getCanvas().getWidth(), buffer.getCanvas().getHeight());
	}

	private void drawRandomHexCodes(GraphicsContext g) {
		clearBuffer();
		g.setFill(Color.LIGHTGRAY);
		g.setFont(ctx.r2D().getArcadeFont());
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
				var hexCode = Integer.toHexString(rnd.nextInt(16));
				g.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		buffer.getCanvas().snapshot(null, currentImage);
	}

	private void drawRandomSprites(GraphicsContext g) {
		clearBuffer();
		var image = ctx.r2D().spritesheetImage();
		var w = image.getWidth();
		var h = image.getHeight();
		var cellSize = 16;
		var numRows = ArcadeWorld.TILES_Y / 2;
		var numCols = ArcadeWorld.TILES_X / 2;
		for (int row = 0; row < numRows; ++row) {
			if (rnd.nextInt(100) > 10) {
				var r1 = new Rectangle2D(rnd.nextDouble(w), rnd.nextDouble(h), cellSize, cellSize);
				var r2 = new Rectangle2D(rnd.nextDouble(w), rnd.nextDouble(h), cellSize, cellSize);
				var split = numCols / 4 + rnd.nextInt(numCols / 2);
				for (int col = 0; col < numCols; ++col) {
					ctx.r2D().drawSprite(g, col < split ? r1 : r2, cellSize * col, cellSize * row);
				}
			}
		}
		buffer.getCanvas().snapshot(null, currentImage);
	}

	private void drawGrid(GraphicsContext g) {
		clearBuffer();
		var cellSize = 16;
		var numRows = ArcadeWorld.TILES_Y / 2;
		var numCols = ArcadeWorld.TILES_X / 2;
		g.setStroke(Color.LIGHTGRAY);
		g.setLineWidth(2.0);
		for (int row = 0; row < numRows; ++row) {
			g.strokeLine(0, row * cellSize, ArcadeWorld.TILES_X * TS, row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			g.strokeLine(col * cellSize, 0, col * cellSize, ArcadeWorld.TILES_Y * TS);
		}
		buffer.getCanvas().snapshot(null, currentImage);
	}
}