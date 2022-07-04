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

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.mspacman.IntroController;
import de.amr.games.pacman.controller.mspacman.IntroController.Context;
import de.amr.games.pacman.controller.mspacman.IntroController.State;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import javafx.scene.canvas.GraphicsContext;
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

	private IntroController sceneController;
	private Context icc;

	@Override
	public void setSceneContext(SceneContext sceneContext) {
		super.setSceneContext(sceneContext);
		sceneController = new IntroController(sceneContext.gameController);
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.START);
		creditVisible = true;
		icc.msPacMan.setAnimations(new PacAnimations(icc.msPacMan, ctx.r2D));
		icc.msPacMan.animations().ifPresent(SpriteAnimations::ensureRunning);
		Stream.of(icc.ghosts).forEach(ghost -> {
			var animations = new GhostAnimations(ghost, ctx.r2D);
			animations.ensureRunning();
			ghost.setAnimations(animations);
		});
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			ctx.state().addCredit(ctx.game());
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			Actions.startGame();
		} else if (Keyboard.pressed(KeyCode.V)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.Z)) {
			ctx.state().startIntermissionTest(ctx.game());
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		creditVisible = icc.creditVisible;
	}

	@Override
	public void doRender(GraphicsContext g) {
		ctx.r2D.drawScore(g, ctx.game().scores.gameScore);
		ctx.r2D.drawScore(g, ctx.game().scores.highScore);
		drawTitle(g);
		drawLights(g, 32, 16);
		if (sceneController.state() == State.GHOSTS) {
			drawGhostText(g, icc.ghosts[icc.ghostIndex]);
		} else if (sceneController.state() == State.MSPACMAN || sceneController.state() == State.READY_TO_PLAY) {
			drawMsPacManText(g);
		}
		ctx.r2D.drawGhosts(g, icc.ghosts);
		ctx.r2D.drawPac(g, icc.msPacMan);
		ctx.r2D.drawCopyright(g, 29);
		if (creditVisible) {
			ctx.r2D.drawCredit(g, ctx.game().credit);
		}
	}

	private void drawTitle(GraphicsContext g) {
		g.setFont(ctx.r2D.getArcadeFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", icc.titlePosition.x, icc.titlePosition.y);
	}

	private void drawGhostText(GraphicsContext g, Ghost ghost) {
		g.setFill(Color.WHITE);
		g.setFont(ctx.r2D.getArcadeFont());
		if (ghost.id == Ghost.RED_GHOST) {
			g.fillText("WITH", icc.titlePosition.x, icc.lightsTopLeft.y + t(3));
		}
		g.setFill(ctx.r2D.getGhostColor(ghost.id));
		g.fillText(ghost.name.toUpperCase(), t(14 - ghost.name.length() / 2), icc.lightsTopLeft.y + t(6));
	}

	private void drawMsPacManText(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.setFont(ctx.r2D.getArcadeFont());
		g.fillText("STARRING", icc.titlePosition.x, icc.lightsTopLeft.y + t(3));
		g.setFill(Color.YELLOW);
		g.fillText("MS PAC-MAN", icc.titlePosition.x, icc.lightsTopLeft.y + t(6));
	}

	private void drawLights(GraphicsContext g, int numDotsX, int numDotsY) {
		long time = icc.lightsTimer.tick();
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
			g.fillRect(icc.lightsTopLeft.x + 4 * x, icc.lightsTopLeft.y + 4 * y, 2, 2);
		}
	}
}