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

import static de.amr.games.pacman.ui.fx.rendering2d.GameRenderer.drawText;

import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private MsPacManIntro intro;
	private final Signature signature = new Signature();

	public MsPacManIntroScene() {
		overlay.getChildren().add(signature.root());
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intro = new MsPacManIntro(context.gameController());
		intro.addStateChangeListener((oldState, newState) -> {
			if (oldState == MsPacManIntro.State.START) {
				signature.show(t(5.5), t(32.0));
			}
		});
		intro.changeState(MsPacManIntro.State.START);

		var msPacAnimations = context.rendererMsPacMan().createPacAnimations(intro.context().msPacMan);
		intro.context().msPacMan.setAnimations(msPacAnimations);
		msPacAnimations.start();
		intro.context().ghosts.forEach(ghost -> {
			var ghostAnimations = context.rendererMsPacMan().createGhostAnimations(ghost);
			ghost.setAnimations(ghostAnimations);
			ghostAnimations.start();
		});

		signature.hide();
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		PacManGames2d.ui.stopVoice();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_ADD_CREDIT) || Keyboard.pressed(PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			PacManGames2d.app.addCredit();
		} else if (Keyboard.pressed(PacManGames2d.KEY_START_GAME)
				|| Keyboard.pressed(PacManGames2d.KEY_START_GAME_NUMPAD)) {
			PacManGames2d.app.startGame();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			PacManGames2d.app.selectNextGameVariant();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			PacManGames2d.app.startCutscenesTest();
		}
	}

	@Override
	protected void drawSceneInfo(GraphicsContext g) {
		GameRenderer.drawTileGrid(g, 28, 36);
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.stopY;
		var r = context.rendererMsPacMan();

		drawMarquee(g);
		drawText(g, "\"MS PAC-MAN\"", ArcadeTheme.ORANGE, PacManGames2d.assets.arcadeFont, tx, ty);
		if (intro.state() == State.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex);
			var color = ArcadeTheme.GHOST_COLORS[ghost.id()].dress();
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText(g, "WITH", ArcadeTheme.PALE, PacManGames2d.assets.arcadeFont, tx, y0 + t(3));
			}
			var text = ghost.name().toUpperCase();
			var dx = text.length() < 4 ? t(1) : 0;
			drawText(g, text, color, PacManGames2d.assets.arcadeFont, tx + t(3) + dx, y0 + t(6));
		} else if (intro.state() == State.MSPACMAN || intro.state() == State.READY_TO_PLAY) {
			drawText(g, "STARRING", ArcadeTheme.PALE, PacManGames2d.assets.arcadeFont, tx, y0 + t(3));
			drawText(g, "MS PAC-MAN", ArcadeTheme.YELLOW, PacManGames2d.assets.arcadeFont, tx, y0 + t(6));
		}
		ic.ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, ic.msPacMan);
		MsPacManGameRenderer.drawCopyright(g, t(6), t(28));
		r.drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
	}

	private void drawMarquee(GraphicsContext g) {
		var on = intro.context().marqueeState();
		for (int i = 0; i < intro.context().numBulbs; ++i) {
			g.setFill(on.get(i) ? ArcadeTheme.PALE : ArcadeTheme.RED);
			if (i <= 33) {
				g.fillRect(60 + 4 * i, 148, 2, 2);
			} else if (i <= 48) {
				g.fillRect(192, 280 - 4 * i, 2, 2);
			} else if (i <= 81) {
				g.fillRect(384 - 4 * i, 88, 2, 2);
			} else {
				g.fillRect(60, 4 * i - 236, 2, 2);
			}
		}
	}
}