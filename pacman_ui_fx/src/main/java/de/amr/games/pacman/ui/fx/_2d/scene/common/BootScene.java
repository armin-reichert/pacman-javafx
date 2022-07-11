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
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.SpritesheetMsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
import de.amr.games.pacman.ui.fx.shell.Actions;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final Random rnd = new Random();
	private Canvas buffer;

	@Override
	public void init() {
		buffer = new Canvas((int) unscaledSize.x, (int) unscaledSize.y);
		clearBuffer();
		Actions.playHelpMessageAfterSeconds(1);
	}

	private void clearBuffer() {
		var g = buffer.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
	}

	@Override
	protected void doUpdate() {
		var g = buffer.getGraphicsContext2D();
		var tick = ctx.state().timer().tick();
		if (between(1.0, 2.0, tick) && tick % 6 == 0) {
			clearBuffer();
			drawHexCodes(g);
		} else if (between(2.0, 3.0, tick) && tick % 6 == 0) {
			clearBuffer();
			drawRandomSprites(g);
		} else if (tick == TickTimer.secToTicks(3.0)) {
			clearBuffer();
			drawGrid(g);
		}
	}

	@Override
	public void drawSceneContent() {
		g.drawImage(buffer.snapshot(null, null), 0, 0);
	}

	private boolean between(double secLeft, double secRight, double tick) {
		return TickTimer.secToTicks(secLeft) <= tick && tick < TickTimer.secToTicks(secRight);
	}

	private void drawHexCodes(GraphicsContext g) {
		g.setFill(Color.LIGHTGRAY);
		g.setFont(SpritesheetPacMan.get().getArcadeFont());
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
				var hexCode = Integer.toHexString(rnd.nextInt(16));
				g.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
		LOGGER.trace("Hex codes");
	}

	private void drawRandomSprites(GraphicsContext g) {
		var sheet = ctx.game().variant == GameVariant.MS_PACMAN ? SpritesheetMsPacMan.get() : SpritesheetPacMan.get();
		var sheetWidth = sheet.getSourceImage().getWidth();
		var sheetHeight = sheet.getSourceImage().getHeight();
		for (int row = 0; row < ArcadeWorld.TILES_Y / 2; ++row) {
			if (rnd.nextInt(100) < 25) {
				continue;
			}
			for (int col = 0; col < ArcadeWorld.TILES_X / 2; ++col) {
				var rect = new Rectangle2D(rnd.nextDouble(sheetWidth), rnd.nextDouble(sheetHeight), 16, 16);
				sheet.drawSprite(g, rect, 16 * col, 16 * row);
			}
		}
		LOGGER.trace("Random sprites");
	}

	private void drawGrid(GraphicsContext g) {
		g.setStroke(Color.LIGHTGRAY);
		g.setLineWidth(2.0);
		for (int row = 0; row < ArcadeWorld.TILES_Y / 2; ++row) {
			g.strokeLine(0, row * 2 * TS, ArcadeWorld.TILES_X * TS, row * 2 * TS);
		}
		for (int col = 0; col < ArcadeWorld.TILES_X / 2; ++col) {
			g.strokeLine(col * 2 * TS, 0, col * 2 * TS, ArcadeWorld.TILES_Y * TS);
		}
		LOGGER.trace("Grid");
	}
}