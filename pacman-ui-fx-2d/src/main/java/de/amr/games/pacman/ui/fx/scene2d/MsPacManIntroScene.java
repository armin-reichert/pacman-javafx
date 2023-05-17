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

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
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
	private Signature signature;

	public MsPacManIntroScene(GameController gameController) {
		super(gameController);
		signature = new Signature();
		signature.add(overlay, 5.5 * TS, 32.0 * TS);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intro = new MsPacManIntro(context.gameController());
		intro.addStateChangeListener((oldState, newState) -> {
			if (oldState == MsPacManIntro.State.START) {
				signature.show();
			}
		});
		intro.changeState(MsPacManIntro.State.START);

		var msPacAnimations = context.rendering2D().createPacAnimations(intro.context().msPacMan);
		intro.context().msPacMan.setAnimations(msPacAnimations);
		msPacAnimations.start();
		intro.context().ghosts.forEach(ghost -> {
			var ghostAnimations = context.rendering2D().createGhostAnimations(ghost);
			ghost.setAnimations(ghostAnimations);
			ghostAnimations.start();
		});

		signature.setOpacity(0); // invisible on start
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		Game2d.ui.stopVoice();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Game2d.KEY_ADD_CREDIT)) {
			Game2d.app.addCredit();
		} else if (Keyboard.pressed(Game2d.KEY_START_GAME)) {
			Game2d.app.startGame();
		} else if (Keyboard.pressed(Game2d.KEY_SELECT_VARIANT)) {
			Game2d.app.selectNextGameVariant();
		} else if (Keyboard.pressed(Game2d.KEY_PLAY_CUTSCENES)) {
			Game2d.app.startCutscenesTest();
		}
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		Rendering2D.drawTileGrid(g, World.TILES_X, World.TILES_Y);
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.stopY;
		var r = (MsPacManGameRenderer) context.rendering2D();

		drawMarquee(g);
		drawText(g, "\"MS PAC-MAN\"", ArcadeTheme.ORANGE, Game2d.assets.arcadeFont, tx, ty);
		if (intro.state() == State.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex);
			var color = ArcadeTheme.GHOST_COLORS[ghost.id()].dress();
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText(g, "WITH", ArcadeTheme.PALE, Game2d.assets.arcadeFont, tx, y0 + TS * 3);
			}
			var text = ghost.name().toUpperCase();
			int dx = text.length() < 4 ? TS : 0;
			drawText(g, text, color, Game2d.assets.arcadeFont, tx + TS * 3 + dx, y0 + TS * 6);
		} else if (intro.state() == State.MSPACMAN || intro.state() == State.READY_TO_PLAY) {
			drawText(g, "STARRING", ArcadeTheme.PALE, Game2d.assets.arcadeFont, tx, y0 + TS * 3);
			drawText(g, "MS PAC-MAN", ArcadeTheme.YELLOW, Game2d.assets.arcadeFont, tx, y0 + TS * 6);
		}
		ic.ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, ic.msPacMan);
		drawCopyright(g);
		r.drawLevelCounter(g, context.game().levelCounter());
	}

	private void drawCopyright(GraphicsContext g) {
		var r = (MsPacManGameRenderer) context.rendering2D();
		r.drawMsPacManCopyright(g, 29);
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