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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntroController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntroState;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
		intro.restart(MsPacManIntroState.START);
		var pacAnimations = context.r2D().createPacAnimations(intro.context().msPacMan);
		pacAnimations.ensureRunning();
		intro.context().msPacMan.setAnimations(pacAnimations);
		intro.context().ghosts.forEach(ghost -> {
			var ghostAnimations = context.r2D().createGhostAnimations(ghost);
			ghostAnimations.ensureRunning();
			ghost.setAnimations(ghostAnimations);
		});
	}

	@Override
	public void update() {
		intro.update();
		context.setCreditVisible(intro.context().isCreditVisible());
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
	public void drawSceneContent(GraphicsContext g) {
		var r = (MsPacManGameRenderer) context.r2D();
		drawTitle(g);
		drawLights(g, 32, 16);
		if (intro.state() == MsPacManIntroState.GHOSTS) {
			drawGhostText(g, intro.context().ghosts.get(intro.context().ghostIndex()));
		} else if (intro.state() == MsPacManIntroState.MSPACMAN || intro.state() == MsPacManIntroState.READY_TO_PLAY) {
			drawMsPacManText(g);
		}
		intro.context().ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, intro.context().msPacMan);
		r.drawCopyright(g, 29);
		r.drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	private void drawTitle(GraphicsContext g) {
		var r = context.r2D();
		drawText(g, "\"MS PAC-MAN\"", Color.ORANGE, r.screenFont(TS), TITLE_TILE.x(), TITLE_TILE.y());
	}

	private void drawGhostText(GraphicsContext g, Ghost ghost) {
		if (ghost.id() == Ghost.ID_RED_GHOST) {
			drawText(g, "WITH", Color.WHITE, context.r2D().screenFont(TS), TITLE_TILE.x(), BLINKY_END_TILE.y() + t(3));
		}
		drawText(g, ghost.name().toUpperCase(), context.r2D().ghostColoring(ghost.id()).normalDress(),
				context.r2D().screenFont(TS), t(14 - ghost.name().length() / 2), BLINKY_END_TILE.y() + t(6));
	}

	private void drawMsPacManText(GraphicsContext g) {
		var r = context.r2D();
		drawText(g, "STARRING", Color.WHITE, r.screenFont(TS), TITLE_TILE.x(), BLINKY_END_TILE.y() + t(3));
		drawText(g, "MS PAC-MAN", Color.YELLOW, r.screenFont(TS), TITLE_TILE.x(), BLINKY_END_TILE.y() + t(6));
	}

	// TODO this is not exactly as in the original game
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