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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.GhostPortrait;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.Scenes;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends AbstractGameScene2D {

	// use exactly same RGB values as sprites
	static final Color PINK = Color.rgb(252, 181, 255);
	static final Color ORANGE = Color.rgb(253, 192, 90);
	static final Color PELLET_COLOR = Color.rgb(254, 189, 180);

	private IntroController sceneController;
	private GameScore2D score2D;
	private GameScore2D hiscore2D;
	private Player2D pacMan2D;
	private List<Ghost2D> ghosts2D;
	private List<Ghost2D> gallery2D;

	public PacMan_IntroScene() {
		super(Scenes.PACMAN_RENDERING, Scenes.PACMAN_SOUNDS, 28, 36);
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		sceneController = new IntroController(gameController);
		sceneController.init();

		score2D = new GameScore2D(rendering);
		score2D.title = "SCORE";
		score2D.x = t(1);
		score2D.y = t(1);
		score2D.levelSupplier = () -> gameController.game().levelNumber;
		score2D.pointsSupplier = () -> gameController.game().score;
		score2D.showPoints = false;

		hiscore2D = new GameScore2D(rendering);
		hiscore2D.title = "HIGH SCORE";
		hiscore2D.x = t(16);
		hiscore2D.y = t(1);
		hiscore2D.levelSupplier = () -> gameController.game().hiscoreLevel;
		hiscore2D.pointsSupplier = () -> gameController.game().hiscorePoints;

		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);

		ghosts2D = Stream.of(sceneController.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, rendering);
			ghost2D.kickingAnimations.values().forEach(TimedSequence::restart);
			ghost2D.frightenedAnimation.restart();
			return ghost2D;
		}).collect(Collectors.toList());

		gallery2D = new ArrayList<>(4);
		for (int i = 0; i < 4; ++i) {
			gallery2D.add(new Ghost2D(sceneController.portraits[i].ghost, rendering));
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		score2D.render(gc);
		hiscore2D.render(gc);
		switch (sceneController.currentStateID) {
		case BEGIN:
		case PRESENTING_GHOSTS:
			drawGallery();
			break;
		case SHOWING_POINTS:
			drawGallery();
			drawPoints(11, 25);
			if (sceneController.stateTimer().ticked() > sec_to_ticks(1)) {
				drawEnergizer();
				drawCopyright(32);
			}
			break;
		case CHASING_PAC:
			drawGallery();
			drawPoints(11, 25);
			drawCopyright(32);
			if (sceneController.blinking.frame()) {
				drawEnergizer();
			}
			drawGuys();
			break;
		case CHASING_GHOSTS:
			drawGallery();
			drawPoints(11, 25);
			drawCopyright(32);
			drawGuys();
			break;
		case READY_TO_PLAY:
			drawGallery();
			drawPressKeyToStart(24);
			break;
		default:
			break;
		}
	}

	private void drawGuys() {
		ghosts2D.forEach(ghost2D -> ghost2D.render(gc));
		pacMan2D.render(gc);
	}

	private void drawGallery() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("CHARACTER", t(6), sceneController.topY);
		gc.fillText("/", t(16), sceneController.topY);
		gc.fillText("NICKNAME", t(18), sceneController.topY);
		for (int i = 0; i < sceneController.portraits.length; ++i) {
			GhostPortrait portrait = sceneController.portraits[i];
			if (portrait.ghost.visible) {
				int y = sceneController.topY + t(1 + 3 * i);
				gallery2D.get(i).render(gc);
				if (portrait.characterVisible) {
					gc.setFill(getGhostColor(i));
					gc.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					gc.setFill(getGhostColor(i));
					gc.fillText("\"" + portrait.ghost.name + "\"", t(17), y + 8);
				}
			}
		}
	}

	private void drawPressKeyToStart(int yTile) {
		if (sceneController.slowBlinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			gc.setFill(Color.WHITE);
			gc.setFont(rendering.getScoreFont());
			gc.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPoints(int tileX, int tileY) {
		gc.setFill(PELLET_COLOR);
		gc.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
		if (sceneController.blinking.frame()) {
			gc.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("10", t(tileX + 2), t(tileY));
		gc.fillText("50", t(tileX + 2), t(tileY + 2));
		gc.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		gc.fillText("PTS", t(tileX + 5), t(tileY));
		gc.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}

	private void drawEnergizer() {
		gc.setFill(PELLET_COLOR);
		gc.fillOval(t(2), t(20), TS, TS);
	}

	private void drawCopyright(int yTile) {
		String text = "\u00A9" + "  1980 MIDWAY MFG. CO.";
		gc.setFont(rendering.getScoreFont());
		gc.setFill(PINK);
		gc.fillText(text, t(3), t(yTile));
	}

	private Color getGhostColor(int id) {
		return id == GameModel.RED_GHOST ? Color.RED
				: id == GameModel.PINK_GHOST ? PINK : id == GameModel.CYAN_GHOST ? Color.CYAN : ORANGE;
	}
}