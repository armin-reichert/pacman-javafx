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

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.Palette;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private static final Vector2i SIZE_TILES = ArcadeWorld.SIZE_TILES;
	private static final Vector2i SIZE_PIXELS = ArcadeWorld.SIZE_PX;
	private static final Random RND = new Random();

	private final GraphicsContext imgCtx;
	private final WritableImage currentImage;

	public BootScene() {
		scoresVisible = false;
		currentImage = new WritableImage(SIZE_PIXELS.x(), SIZE_PIXELS.y());
		imgCtx = new Canvas(SIZE_PIXELS.x(), SIZE_PIXELS.y()).getGraphicsContext2D();
	}

	@Override
	public void init() {
		clearImage();
		saveImage();
	}

	@Override
	public void update() {
		var timer = context.state().timer();
		if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 5 == 0) {
			produceRandomHexCodesImage();
		} else if (timer.betweenSeconds(2.0, 3.5) && timer.tick() % 5 == 0) {
			produceRandomSpriteImage();
		} else if (timer.atSecond(3.5)) {
			produceGridImage();
		} else if (timer.atSecond(4.0)) {
			context.gameController().terminateCurrentState();
		}
	}

	@Override
	public void drawSceneContent() {
		g.drawImage(currentImage, 0, 0);
	}

	private void clearImage() {
		imgCtx.setFill(Color.BLACK);
		imgCtx.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
	}

	private void saveImage() {
		imgCtx.getCanvas().snapshot(null, currentImage);
	}

	private void produceRandomHexCodesImage() {
		clearImage();
		imgCtx.setFill(Palette.PALE);
		imgCtx.setFont(context.r2D().screenFont(TS));
		for (int row = 0; row < SIZE_TILES.y(); ++row) {
			for (int col = 0; col < SIZE_TILES.x(); ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				imgCtx.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		saveImage();
	}

	private void produceRandomSpriteImage() {
		clearImage();
		if (context.r2D() instanceof SpritesheetGameRenderer sgr) {
			for (int row = 0; row < SIZE_TILES.y() / 2; ++row) {
				if (RND.nextInt(100) > 10) {
					var r1 = sgr.spritesheet().randomCell();
					var r2 = sgr.spritesheet().randomCell();
					var splitX = SIZE_TILES.x() / 8 + RND.nextInt(SIZE_TILES.x() / 4);
					for (int col = 0; col < SIZE_TILES.x() / 2; ++col) {
						var r = col < splitX ? r1 : r2;
						sgr.drawSprite(imgCtx, r, r.getWidth() * col, r.getHeight() * row);
					}
				}
			}
		}
		saveImage();
	}

	private void produceGridImage() {
		clearImage();
		var cellSize = 16;
		var numRows = SIZE_TILES.y() / 2;
		var numCols = SIZE_TILES.x() / 2;
		imgCtx.setStroke(Palette.PALE);
		imgCtx.setLineWidth(2.0);
		for (int row = 0; row <= numRows; ++row) {
			imgCtx.strokeLine(0, row * cellSize, SIZE_PIXELS.x(), row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			imgCtx.strokeLine(col * cellSize, 0, col * cellSize, SIZE_PIXELS.y());
		}
		saveImage();
	}
}