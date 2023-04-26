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
package de.amr.games.pacman.ui.fx._2d.scene;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawText;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntroController;
import de.amr.games.pacman.controller.MsPacManIntroState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
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
	protected void drawInfo(GraphicsContext g) {
		Rendering2D.drawTileStructure(g, World.TILES_X, World.TILES_Y);
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.blinkyEndPosition.y();
		var r = (MsPacManGameRenderer) context.rendering2D();
		var font = r.screenFont(TS);

		drawMarquee(g);
		drawText(g, "\"MS PAC-MAN\"", ArcadeTheme.ORANGE, font, tx, ty);
		if (intro.state() == MsPacManIntroState.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex);
			var color = r.ghostColors(ghost.id()).dress();
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText(g, "WITH", ArcadeTheme.PALE, font, tx, y0 + TS * 3);
			}
			drawText(g, ghost.name().toUpperCase(), color, font, TS * (16 - 0.5 * ghost.name().length()), y0 + TS * 6);
		} else if (intro.state() == MsPacManIntroState.MSPACMAN || intro.state() == MsPacManIntroState.READY_TO_PLAY) {
			drawText(g, "STARRING", ArcadeTheme.PALE, font, tx, y0 + TS * 3);
			drawText(g, "MS PAC-MAN", ArcadeTheme.YELLOW, font, tx, y0 + TS * 6);
		}
		ic.ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, ic.msPacMan);
		r.drawCopyright(g, 29);
		drawLevelCounter(g);
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