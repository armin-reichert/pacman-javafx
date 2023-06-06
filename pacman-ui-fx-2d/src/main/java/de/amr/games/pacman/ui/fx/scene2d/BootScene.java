/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
		setRoundedCorners(false);
		context.setScoreVisible(false);
	}

	@Override
	public void end() {
		context.ui().soundHandler().playVoice("voice.explain", 1.5f);
	}

	@Override
	public void render() {
		var timer = context.state().timer();
		if (timer.tick() == 1) {
			drawSceneBackground();
		} else if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 8 == 0) {
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
				g.fillText(hexCode, s(t(col)), s(t(row + 1)));
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
					drawSprite(region, region.getWidth() * col, region.getHeight() * row);
				}
			}
		}
	}

	private Rectangle2D randomSpritesheetTile() {
		var source = context.ui().spritesheet().source();
		var raster = context.ui().spritesheet().raster();
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
			g.strokeLine(0, s(row * raster), s(WIDTH_UNSCALED), s(row * raster));
		}
		for (int col = 0; col <= numCols; ++col) {
			g.strokeLine(s(col * raster), 0, s(col * raster), s(HEIGHT_UNSCALED));
		}
	}
}