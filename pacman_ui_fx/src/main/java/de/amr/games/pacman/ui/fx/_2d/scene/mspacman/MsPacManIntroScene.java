/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.mspacman.MsPacManIntro;
import de.amr.games.pacman.controller.mspacman.MsPacManIntro.IntroState;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private MsPacManIntro intro;

	@Override
	public void init() {
		intro = new MsPacManIntro(ctx.gameController());
		intro.restart(MsPacManIntro.IntroState.START);
		var pacAnimations = ctx.r2D().createPacAnimationSet(ctx.game().pac());
		pacAnimations.ensureRunning();
		ctx.game().pac().setAnimationSet(pacAnimations);
		ctx.game().ghosts().forEach(ghost -> {
			var ghostAnimations = ctx.r2D().createGhostAnimationSet(ghost);
			ghostAnimations.ensureRunning();
			ghost.setAnimationSet(ghostAnimations);
		});
	}

	@Override
	public void update() {
		intro.update();
		setCreditVisible(intro.context().creditVisible);
	}

	@Override
	public void end() {
		Actions.stopVoiceMessage();
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			Actions.startGame();
		} else if (Keyboard.pressed(KeyCode.V)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.Z)) {
			Actions.startCutscenesTest();
		}
	}

	@Override
	public void draw() {
		drawTitle();
		drawLights(32, 16);
		if (intro.state() == IntroState.GHOSTS) {
			drawGhostText(ctx.game().ghost(intro.context().ghostIndex));
		} else if (intro.state() == IntroState.MSPACMAN || intro.state() == IntroState.READY_TO_PLAY) {
			drawMsPacManText();
		}
		ctx.game().ghosts().forEach(ghost -> ctx.r2D().drawGhost(g, ghost));
		ctx.r2D().drawPac(g, ctx.game().pac());
		ctx.r2D().drawCopyright(g, 29);
		if (ctx.game().credit() > 0) {
			ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter());
		}
	}

	private void drawTitle() {
		ctx.r2D().drawText(g, "\"MS PAC-MAN\"", Color.ORANGE, ctx.r2D().arcadeFont(TS), intro.context().titlePosition.x(),
				intro.context().titlePosition.y());
	}

	private void drawGhostText(Ghost ghost) {
		if (ghost.id() == Ghost.ID_RED_GHOST) {
			ctx.r2D().drawText(g, "WITH", Color.WHITE, ctx.r2D().arcadeFont(TS), intro.context().titlePosition.x(),
					intro.context().redGhostEndPosition.y() + t(3));
		}
		ctx.r2D().drawText(g, ghost.name().toUpperCase(), ctx.r2D().ghostColor(ghost.id()), ctx.r2D().arcadeFont(TS),
				t(14 - ghost.name().length() / 2), intro.context().redGhostEndPosition.y() + t(6));
	}

	private void drawMsPacManText() {
		ctx.r2D().drawText(g, "STARRING", Color.WHITE, ctx.r2D().arcadeFont(TS), intro.context().titlePosition.x(),
				intro.context().redGhostEndPosition.y() + t(3));
		ctx.r2D().drawText(g, "MS PAC-MAN", Color.YELLOW, ctx.r2D().arcadeFont(TS), intro.context().titlePosition.x(),
				intro.context().redGhostEndPosition.y() + t(6));
	}

	// TODO this is not yet accurate to the original game
	private void drawLights(int numDotsX, int numDotsY) {
		long time = intro.context().lightsTimer.tick();
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0;
			int y = 0;
			if (dot <= numDotsX) {
				x = dot;
			} else if (dot < numDotsX + numDotsY) {
				x = numDotsX;
				y = dot - numDotsX;
			} else if (dot < 2 * numDotsX + numDotsY + 1) {
				x = 2 * numDotsX + numDotsY - dot;
				y = numDotsY;
			} else {
				y = 2 * (numDotsX + numDotsY) - dot;
			}
			g.setFill((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(intro.context().redGhostEndPosition.x() + 4 * x, intro.context().redGhostEndPosition.y() + 4 * y, 2,
					2);
		}
	}
}