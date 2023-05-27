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

package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.RND;

import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	@Override
	public void init() {
		setSceneCanvasScaled(true);
		context.setScoreVisible(false);
	}

	@Override
	public void render() {
		var timer = context.state().timer();
		if (timer.tick() == 1) {
			drawSceneBackground();
		} else if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 4 == 0) {
			drawSceneBackground();
			paintRandomHexCodes();
		} else if (timer.betweenSeconds(2.0, 3.5) && timer.tick() % 4 == 0) {
			drawSceneBackground();
			paintRandomSprites();
		} else if (timer.atSecond(3.5)) {
			drawSceneBackground();
			paintGrid(16);
		} else if (timer.atSecond(4.0)) {
			context.gameController().terminateCurrentState();
		}
	}

	@Override
	protected void drawSceneContent() {
		// not used here
	}

	private void paintRandomHexCodes() {
		g.setFill(ArcadeTheme.PALE);
		g.setFont(sceneFont());
		for (int row = 0; row < TILES_Y; ++row) {
			for (int col = 0; col < TILES_X; ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				g.fillText(hexCode, t(col), t(row + 1));
			}
		}
	}

	private void paintRandomSprites() {
		for (int row = 0; row < TILES_Y / 2; ++row) {
			if (RND.nextInt(100) > 10) {
				var region1 = randomSpritesheetTile();
				var region2 = randomSpritesheetTile();
				var splitX = TILES_X / 8 + RND.nextInt(TILES_X / 4);
				for (int col = 0; col < TILES_X / 2; ++col) {
					var region = col < splitX ? region1 : region2;
					r().drawSprite(g, region, region.getWidth() * col, region.getHeight() * row);
				}
			}
		}
	}

	private Rectangle2D randomSpritesheetTile() {
		var source = r().spritesheet().source();
		var raster = r().spritesheet().raster();
		double x = RND.nextDouble() * (source.getWidth() - raster);
		double y = RND.nextDouble() * (source.getHeight() - raster);
		return new Rectangle2D(x, y, raster, raster);
	}

	private void paintGrid(int raster) {
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		g.setStroke(ArcadeTheme.PALE);
		g.setLineWidth(2.0);
		for (int row = 0; row <= numRows; ++row) {
			g.strokeLine(0, row * raster, WIDTH_UNSCALED, row * raster);
		}
		for (int col = 0; col <= numCols; ++col) {
			g.strokeLine(col * raster, 0, col * raster, HEIGHT_UNSCALED);
		}
	}
}