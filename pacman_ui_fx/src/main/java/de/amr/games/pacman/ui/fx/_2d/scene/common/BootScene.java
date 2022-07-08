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

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	private final Random rnd = new Random();

	@Override
	protected void doUpdate() {
		// nothing to update
	}

	@Override
	public void doRender(GraphicsContext g) {
		var tick = ctx.state().timer().tick();
		if (ctx.state() == GameState.BOOT) {
			if (between(1.0, 2.0, tick)) {
				drawHexCodes(g, tick);
			} else if (between(2.0, 3.0, tick)) {
				drawRandomSprites(g, tick);
			} else if (between(3.0, 4.0, tick)) {
				drawGrid(g);
			}
		}
	}

	private boolean between(double secLeft, double secRight, double tick) {
		return TickTimer.secToTicks(secLeft) <= tick && tick < TickTimer.secToTicks(secRight);
	}

	private void drawHexCodes(GraphicsContext g, long tick) {
		g.setFill(Color.LIGHTGRAY);
		g.setFont(SpritesheetPacMan.get().getArcadeFont());
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
				var hexCode = Integer.toHexString(rnd.nextInt(16));
				g.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
	}

	private void drawRandomSprites(GraphicsContext g, long tick) {
		for (int row = 0; row < ArcadeWorld.TILES_Y / 2; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X / 2; ++col) {
				var x = rnd.nextInt(14);
				var y = rnd.nextInt(10);
				var sprite = SpritesheetPacMan.get().subImage(x * 16, y * 16 + 8, 16, 16);
				g.drawImage(sprite, col * 2 * TS, row * 2 * TS);
			}
		}
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
	}
}