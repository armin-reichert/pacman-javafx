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

import de.amr.games.pacman.model.common.GameVariant;
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

	private static final int TILES_X = ArcadeWorld.SIZE_TILES.x();
	private static final int TILES_Y = ArcadeWorld.SIZE_TILES.y();
	private static final int SIZE_X = ArcadeWorld.SIZE_PX.x();
	private static final int SIZE_Y = ArcadeWorld.SIZE_PX.y();
	private static final Random RND = new Random();

	private final GraphicsContext gc;
	private final WritableImage currentImage;

	public BootScene() {
		currentImage = new WritableImage(SIZE_X, SIZE_Y);
		gc = new Canvas(SIZE_X, SIZE_Y).getGraphicsContext2D();
	}

	@Override
	public void init() {
		clearBuffer();
		takeSnapshot();
	}

	@Override
	public void update() {
		var timer = ctx.state().timer();
		if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 5 == 0) {
			drawRandomHexCodes();
		} else if (timer.betweenSeconds(2.0, 3.5) && timer.tick() % 5 == 0) {
			drawRandomSprites();
		} else if (timer.atSecond(3.5)) {
			drawGrid();
		} else if (timer.atSecond(4.0)) {
			ctx.gameController().terminateCurrentState();
		}
	}

	@Override
	public void draw() {
		g.drawImage(currentImage, 0, 0);
	}

	@Override
	public void drawHUD() {
		// hide HUD
	}

	private void clearBuffer() {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
	}

	private void takeSnapshot() {
		gc.getCanvas().snapshot(null, currentImage);
	}

	private void drawRandomHexCodes() {
		clearBuffer();
		gc.setFill(Color.rgb(222, 222, 255));
		gc.setFont(ctx.r2D().arcadeFont(TS));
		for (int row = 0; row < TILES_Y; ++row) {
			for (int col = 0; col < TILES_X; ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				gc.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		takeSnapshot();
	}

	private void drawRandomSprites() {
		clearBuffer();
		var sheet = ctx.r2D().spritesheet();
		var image = sheet.source();
		if (ctx.gameVariant() == GameVariant.MS_PACMAN) {
			image = sheet.region(0, 0, (int) image.getWidth(), 248);
		}
		var w = image.getWidth();
		var h = image.getHeight();
		var cellSize = 16;
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		for (int row = 0; row < numRows; ++row) {
			if (RND.nextInt(100) < 10) {
				continue;
			}
			var r1 = new Rectangle2D(RND.nextDouble(w), RND.nextDouble(h), cellSize, cellSize);
			var r2 = new Rectangle2D(RND.nextDouble(w), RND.nextDouble(h), cellSize, cellSize);
			var split = numCols / 4 + RND.nextInt(numCols / 2);
			for (int col = 0; col < numCols; ++col) {
				ctx.r2D().drawSprite(gc, col < split ? r1 : r2, cellSize * col, cellSize * row);
			}
		}
		takeSnapshot();
	}

	private void drawGrid() {
		clearBuffer();
		var cellSize = 16;
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		gc.setStroke(Color.rgb(222, 222, 255));
		gc.setLineWidth(2.0);
		for (int row = 0; row <= numRows; ++row) {
			gc.strokeLine(0, row * cellSize, SIZE_X, row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			gc.strokeLine(col * cellSize, 0, col * cellSize, SIZE_Y);
		}
		takeSnapshot();
	}
}