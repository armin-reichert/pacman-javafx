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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.IntroController;
import de.amr.games.pacman.controller.mspacman.IntroController.Context;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends GameScene2D {

	private final IntroController sceneController;
	private final Context context;

	private Player2D msPacMan2D;
	private Ghost2D[] ghosts2D;

	public MsPacMan_IntroScene(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new IntroController(gameController);
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.BEGIN);
		createCommonParts(game);
		score2D.showPoints = false;
		credit2D.visible = true;
		msPacMan2D = new Player2D(context.msPacMan, game).createAnimations(r2D);
		msPacMan2D.animMunching.values().forEach(SpriteAnimation::restart);
		ghosts2D = Stream.of(context.ghosts).map(ghost -> new Ghost2D(ghost, game, r2D)).toArray(Ghost2D[]::new);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animKicking.values().forEach(SpriteAnimation::restart));
	}

	@Override
	public void handleKeyPressed(KeyEvent e) {
		if (GameUI.pressed(e, KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
			return;
		} else if (GameUI.pressed(e, KeyCode.SPACE)) {
			gameController.requestGame();
			return;
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		score2D.render(g, r2D);
		highScore2D.render(g, r2D);
		credit2D.render(g, r2D);
		g.setFont(r2D.getArcadeFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", context.titlePosition.x, context.titlePosition.y);
		drawAnimatedBoard(g, 32, 16);
		switch (sceneController.state()) {
		case GHOSTS -> drawGhostText(g, context.ghosts[context.ghostIndex]);
		case MSPACMAN, READY_TO_PLAY -> drawMsPacManText(g);
		default -> {
		}
		}
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(g, r2D));
		msPacMan2D.render(g, r2D);
		r2D.drawCopyright(g, t(3), t(28));
	}

	private void drawGhostText(GraphicsContext g, Ghost ghost) {
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		if (ghost.id == Ghost.RED_GHOST) {
			g.fillText("WITH", context.titlePosition.x, context.lightsTopLeft.y + t(3));
		}
		g.setFill(r2D.getGhostColor(ghost.id));
		g.fillText(ghost.name.toUpperCase(), t(14 - ghost.name.length() / 2), context.lightsTopLeft.y + t(6));
	}

	private void drawMsPacManText(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("STARRING", context.titlePosition.x, context.lightsTopLeft.y + t(3));
		g.setFill(Color.YELLOW);
		g.fillText("MS PAC-MAN", context.titlePosition.x, context.lightsTopLeft.y + t(6));
	}

	private void drawAnimatedBoard(GraphicsContext g, int numDotsX, int numDotsY) {
		long time = context.lightsTimer.tick();
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0, y = 0;
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
			g.fillRect(context.lightsTopLeft.x + 4 * x, context.lightsTopLeft.y + 4 * y, 2, 2);
		}
	}
}