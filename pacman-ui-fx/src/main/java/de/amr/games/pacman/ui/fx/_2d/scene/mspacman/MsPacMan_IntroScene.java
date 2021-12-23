/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.mspacman.IntroController;
import de.amr.games.pacman.controller.mspacman.IntroController.IntroState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.Scenes;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends AbstractGameScene2D {

	private IntroController sceneController;
	private TickTimer boardAnimationTimer = new TickTimer("boardAnimation-timer");
	private Player2D msPacMan2D;
	private List<Ghost2D> ghosts2D;

	public MsPacMan_IntroScene() {
		super(Scenes.MS_PACMAN_RENDERING, Scenes.MS_PACMAN_SOUNDS, 28, 36);
	}

	@Override
	public void init() {
		boardAnimationTimer.reset();
		boardAnimationTimer.start();
		sceneController = new IntroController(gameController);
		sceneController.init();
		msPacMan2D = new Player2D(sceneController.msPacMan, rendering);
		msPacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		ghosts2D = Stream.of(sceneController.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, rendering);
			ghost2D.kickingAnimations.values().forEach(TimedSequence::restart);
			return ghost2D;
		}).collect(Collectors.toList());
	}

	@Override
	public void end() {
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		boardAnimationTimer.tick();
	}

	@Override
	public void doRender() {
		gc.setFont(rendering.getScoreFont());
		gc.setFill(Color.ORANGE);
		gc.fillText("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedBoard(32, 16);
		if (sceneController.currentStateID == IntroState.PRESENTING_GHOST) {
			drawPresentingGhost(sceneController.ghosts[sceneController.currentGhostIndex]);
		} else if (sceneController.currentStateID == IntroState.PRESENTING_MSPACMAN) {
			drawStarringMsPacMan();
		} else if (sceneController.currentStateID == IntroState.WAITING_FOR_GAME) {
			drawStarringMsPacMan();
			drawPointsAnimation(26);
			drawPressKeyToStart(32);
		}
		ghosts2D.forEach(ghost2D -> ghost2D.render(gc));
		msPacMan2D.render(gc);
	}

	private void drawPresentingGhost(Ghost ghost) {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		if (ghost == sceneController.ghosts[0]) {
			gc.fillText("WITH", t(8), t(11));
		}
		gc.setFill(ghost.id == 0 ? Color.RED : ghost.id == 1 ? Color.PINK : ghost.id == 2 ? Color.CYAN : Color.ORANGE);
		gc.fillText(ghost.name.toUpperCase(), t(13 - ghost.name.length() / 2), t(14));
	}

	private void drawStarringMsPacMan() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("STARRING", t(8), t(11));
		gc.setFill(Color.YELLOW);
		gc.fillText("MS PAC-MAN", t(8), t(14));
	}

	private void drawAnimatedBoard(int numDotsX, int numDotsY) {
		long time = boardAnimationTimer.ticked();
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
			gc.setFill((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			gc.fillRect(t(sceneController.tileBoardTopLeft.x) + 4 * x, t(sceneController.tileBoardTopLeft.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(int tileY) {
		if (sceneController.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			gc.setFill(Color.ORANGE);
			gc.setFont(rendering.getScoreFont());
			gc.fillText(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawPointsAnimation(int tileY) {
		int x = t(10), y = t(tileY);
		if (sceneController.blinking.frame()) {
			gc.setFill(Color.PINK);
			gc.fillOval(x, y + t(1) - 2, 10, 10);
			gc.fillRect(x + 6, y - t(1) + 2, 2, 2);
		}
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("10", x + t(2), y);
		gc.fillText("50", x + t(2), y + t(2));
		gc.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		gc.fillText("PTS", x + t(5), y);
		gc.fillText("PTS", x + t(5), y + t(2));
	}
}