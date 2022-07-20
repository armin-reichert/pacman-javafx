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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.ArcadeRendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.ArcadeRendererPacManGame;
import de.amr.games.pacman.ui.fx.shell.Actions;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final Random rnd = new Random();
	private final Canvas buffer;
	private final GraphicsContext bg;
	private Image spritesheetImage;

	public BootScene() {
		buffer = new Canvas(unscaledSize.x(), unscaledSize.y());
		bg = buffer.getGraphicsContext2D();
	}

	@Override
	public void init() {
		clearBuffer();
		Actions.playHelpMessageAfterSeconds(1);
		spritesheetImage = ctx.gameVariant() == GameVariant.MS_PACMAN //
				? ArcadeRendererMsPacManGame.get().getSheet().getSourceImage()
				: ArcadeRendererPacManGame.get().getSheet().getSourceImage();
	}

	@Override
	public void update() {
		var t = ctx.state().timer().tick();
		if (betweenSec(0.5, 1.5, t) && t % 5 == 0) {
			clearBuffer();
			drawRandomHexCodesIntoBuffer();
		} else if (betweenSec(1.5, 4.0, t) && t % 10 == 0) {
			clearBuffer();
			drawRandomSpritesIntoBuffer(spritesheetImage);
		} else if (t == TickTimer.secToTicks(4.0)) {
			clearBuffer();
			drawGridIntoBuffer();
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		g.drawImage(buffer.snapshot(null, null), 0, 0);
	}

	@Override
	public void drawHUD(GraphicsContext g, Font hudFont) {
		// no HUD
	}

	private void clearBuffer() {
		bg.setFill(Color.BLACK);
		bg.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
	}

	private boolean betweenSec(double secLeft, double secRight, double tick) {
		return TickTimer.secToTicks(secLeft) <= tick && tick < TickTimer.secToTicks(secRight);
	}

	private void drawRandomHexCodesIntoBuffer() {
		bg.setFill(Color.LIGHTGRAY);
		bg.setFont(ctx.r2D.getArcadeFont());
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
				var hexCode = Integer.toHexString(rnd.nextInt(16));
				bg.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		LOGGER.trace("Hex codes");
	}

	private void drawRandomSpritesIntoBuffer(Image sheetImage) {
		var w = sheetImage.getWidth();
		var h = sheetImage.getHeight();
		var cellSize = 16;
		var numRows = ArcadeWorld.TILES_Y / 2;
		var numCols = ArcadeWorld.TILES_X / 2;
		for (int row = 0; row < numRows; ++row) {
			if (rnd.nextInt(100) > 10) {
				var r1 = new Rectangle2D(rnd.nextDouble(w), rnd.nextDouble(h), cellSize, cellSize);
				var r2 = new Rectangle2D(rnd.nextDouble(w), rnd.nextDouble(h), cellSize, cellSize);
				var split = numCols / 4 + rnd.nextInt(numCols / 2);
				for (int col = 0; col < numCols; ++col) {
					ctx.r2D.drawSprite(bg, col < split ? r1 : r2, cellSize * col, cellSize * row);
				}
			}
		}
		LOGGER.trace("Random sprites");
	}

	private void drawGridIntoBuffer() {
		var cellSize = 16;
		var numRows = ArcadeWorld.TILES_Y / 2;
		var numCols = ArcadeWorld.TILES_X / 2;
		bg.setStroke(Color.LIGHTGRAY);
		bg.setLineWidth(2.0);
		for (int row = 0; row < numRows; ++row) {
			bg.strokeLine(0, row * cellSize, ArcadeWorld.TILES_X * TS, row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			bg.strokeLine(col * cellSize, 0, col * cellSize, ArcadeWorld.TILES_Y * TS);
		}
		LOGGER.trace("Grid");
	}
}