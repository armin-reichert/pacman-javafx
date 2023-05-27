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

import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;

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
	protected MsPacManGameRenderer r() {
		return (MsPacManGameRenderer) super.r();
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

		var msPacAnimations = r().createPacAnimations(intro.context().msPacMan);
		intro.context().msPacMan.setAnimations(msPacAnimations);
		msPacAnimations.start();
		intro.context().ghosts.forEach(ghost -> {
			var ghostAnimations = r().createGhostAnimations(ghost);
			ghost.setAnimations(ghostAnimations);
			ghostAnimations.start();
		});

		signature.setNameFont(context.ui().theme().font("font.handwriting", 9));
		signature.hide();
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		context.ui().stopVoice();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			context.ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			context.ui().startGame();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			context.ui().selectNextGameVariant();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			context.ui().startCutscenesTest();
		}
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(28, 36);
	}

	@Override
	public void drawSceneContent() {
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.stopY;
		drawMarquee();
		drawText("\"MS PAC-MAN\"", ArcadeTheme.ORANGE, sceneFont(), tx, ty);
		if (intro.state() == State.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex);
			var color = context.ui().theme().color(String.format("ghost.%d.color.normal.dress", ghost.id()));
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText("WITH", ArcadeTheme.PALE, sceneFont(), tx, y0 + t(3));
			}
			var text = ghost.name().toUpperCase();
			var dx = text.length() < 4 ? t(1) : 0;
			drawText(text, color, sceneFont(), tx + t(3) + dx, y0 + t(6));
		} else if (intro.state() == State.MSPACMAN || intro.state() == State.READY_TO_PLAY) {
			drawText("STARRING", ArcadeTheme.PALE, sceneFont(), tx, y0 + t(3));
			drawText("MS PAC-MAN", ArcadeTheme.YELLOW, sceneFont(), tx, y0 + t(6));
		}
		ic.ghosts.forEach(ghost -> drawGhostSprite(ghost));
		drawPacSprite(ic.msPacMan);
		drawMsPacManCopyright(t(6), t(28));
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}

	private void drawMarquee() {
		var on = intro.context().marqueeState();
		for (int i = 0; i < intro.context().numBulbs; ++i) {
			g.setFill(on.get(i) ? ArcadeTheme.PALE : ArcadeTheme.RED);
			if (i <= 33) {
				g.fillRect(s(60 + 4 * i), s(148), s(2), s(2));
			} else if (i <= 48) {
				g.fillRect(s(192), s(280 - 4 * i), s(2), s(2));
			} else if (i <= 81) {
				g.fillRect(s(384 - 4 * i), s(88), s(2), s(2));
			} else {
				g.fillRect(s(60), s(4 * i - 236), s(2), s(2));
			}
		}
	}
}