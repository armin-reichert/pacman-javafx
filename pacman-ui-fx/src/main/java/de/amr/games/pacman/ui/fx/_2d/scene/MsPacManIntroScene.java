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
import static de.amr.games.pacman.lib.Globals.isOdd;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawText;

import java.util.BitSet;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntroController;
import de.amr.games.pacman.controller.MsPacManIntroState;
import de.amr.games.pacman.model.actors.Ghost;
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
	public void drawScene(GraphicsContext g) {
		var ic = intro.context();
		var r = (MsPacManGameRenderer) context.rendering2D();
		var font = r.screenFont(TS);
		drawLightChain(g);
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.blinkyEndPosition.y();
		drawText(g, "\"MS PAC-MAN\"", ArcadeTheme.ORANGE, font, tx, ty);
		if (intro.state() == MsPacManIntroState.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex());
			var ghostColor = r.ghostColors(ghost.id()).dress();
			if (ghost.id() == Ghost.ID_RED_GHOST) {
				drawText(g, "WITH", ArcadeTheme.PALE, font, tx, y0 + TS * (3));
			}
			drawText(g, ghost.name().toUpperCase(), ghostColor, font, TS * (14 - ghost.name().length() / 2), y0 + TS * (6));
		} else if (intro.state() == MsPacManIntroState.MSPACMAN || intro.state() == MsPacManIntroState.READY_TO_PLAY) {
			drawText(g, "STARRING", ArcadeTheme.PALE, font, tx, y0 + TS * (3));
			drawText(g, "MS PAC-MAN", ArcadeTheme.YELLOW, font, tx, y0 + TS * (6));
		}
		ic.ghosts.forEach(ghost -> r.drawGhost(g, ghost));
		r.drawPac(g, ic.msPacMan);
		r.drawCopyright(g, 29);
		drawLevelCounter(g);
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		Rendering2D.drawTileStructure(g, World.TILES_X, World.TILES_Y);
	}

	private void drawLightChain(GraphicsContext g) {
		var t = intro.context().marqueeTimer.tick();

		var on = new BitSet(96);
		// 6 bulbs with distance 16 are on each frame
		for (int k = 0; k < 6; ++k) {
			var i = (int) (t + k * 16) % 96;
			on.set(i);
		}

		int x0 = 14;
		int y0 = 21;
		int x, y;
		for (int i = 0; i < 96; ++i) {
			// In the Arcade game, the bulbs in the leftmost column are all off every second frame. Maybe a bug?
			if (i > 80 && isOdd(i)) {
				on.set(i, false);
			}
			if (i <= 33) {
				x = x0 + i;
				y = y0 + 15;
			} else if (i <= 48) {
				x = x0 + 33;
				y = y0 + 48 - i;
			} else if (i <= 81) {
				x = x0 + 81 - i;
				y = y0;
			} else {
				x = x0;
				y = y0 + i - 81;
			}
			g.setFill(on.get(i) ? ArcadeTheme.PALE : ArcadeTheme.RED);
			g.fillRect(4 * x + 4, 4 * y + 4, 2, 2);
		}
	}
}