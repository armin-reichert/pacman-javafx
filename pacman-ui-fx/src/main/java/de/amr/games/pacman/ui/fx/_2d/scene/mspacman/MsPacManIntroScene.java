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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.BLINKY_END_TILE;
import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.TITLE_TILE;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D.drawText;
import static de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D.drawTileStructure;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntroController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntroState;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private MsPacManIntroController intro;

	public MsPacManIntroScene(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intro = new MsPacManIntroController(context.gameController());
		intro.changeState(MsPacManIntroState.START);

		var msPacAnimations = context.rendering2D().createPacAnimations(intro.context().msPacMan);
		intro.context().msPacMan.setAnimations(msPacAnimations);
		msPacAnimations.start();
		intro.context().ghosts.forEach(ghost -> {
			var ghostAnimations = context.rendering2D().createGhostAnimations(ghost);
			ghost.setAnimations(ghostAnimations);
			ghostAnimations.start();
		});
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		Actions.stopVoiceMessage();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.ADD_CREDIT)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(Keys.START_GAME)) {
			Actions.startGame();
		} else if (Keyboard.pressed(Keys.SELECT_VARIANT)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Keys.PLAY_CUTSCENES)) {
			Actions.startCutscenesTest();
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var ic = intro.context();
		var r = (MsPacManGameRenderer) context.rendering2D();
		var font = r.screenFont(TS);
		drawLights(g, 32, 16);
		drawText(g, "\"MS PAC-MAN\"", ArcadeTheme.ORANGE, font, TITLE_TILE.x(), TITLE_TILE.y());
		if (intro.state() == MsPacManIntroState.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex());
			var ghostColor = r.ghostColoring(ghost.id()).normalDress();
			if (ghost.id() == Ghost.ID_RED_GHOST) {
				drawText(g, "WITH", ArcadeTheme.PALE, font, TITLE_TILE.x(), BLINKY_END_TILE.y() + t(3));
			}
			drawText(g, ghost.name().toUpperCase(), ghostColor, font, t(14 - ghost.name().length() / 2),
					BLINKY_END_TILE.y() + t(6));
		} else if (intro.state() == MsPacManIntroState.MSPACMAN || intro.state() == MsPacManIntroState.READY_TO_PLAY) {
			drawText(g, "STARRING", ArcadeTheme.PALE, font, TITLE_TILE.x(), BLINKY_END_TILE.y() + t(3));
			drawText(g, "MS PAC-MAN", ArcadeTheme.YELLOW, font, TITLE_TILE.x(), BLINKY_END_TILE.y() + t(6));
		}
		ic.ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, ic.msPacMan);
		r.drawCopyright(g, 29);
		drawLevelCounter(g);
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		drawTileStructure(g, ArcadeWorld.SIZE_TILES.x(), ArcadeWorld.SIZE_TILES.y());
	}

	// TODO this is not exactly as in the original game, but looks quite ok
	private void drawLights(GraphicsContext g, int width, int height) {
		long t = intro.context().lightsTimer.tick();
		int on = (int) t % (width / 2);
		for (int i = 0; i < 2 * (width + height); ++i) {
			var p = xy(i, width, height);
			g.setFill(p.x() > 0 && (i + on) % (width / 2) == 0 ? ArcadeTheme.PALE : ArcadeTheme.RED);
			g.fillRect(BLINKY_END_TILE.x() + 4 * p.x(), BLINKY_END_TILE.y() + 4 * p.y(), 2, 2);
		}
	}

	private Vector2i xy(int i, int width, int height) {
		if (i <= width) {
			return new Vector2i(i, 0);
		} else if (i < width + height) {
			return new Vector2i(width, i - width);
		} else if (i < 2 * width + height + 1) {
			return new Vector2i(2 * width + height - i, height);
		} else {
			return new Vector2i(0, 2 * (width + height) - i);
		}
	}
}