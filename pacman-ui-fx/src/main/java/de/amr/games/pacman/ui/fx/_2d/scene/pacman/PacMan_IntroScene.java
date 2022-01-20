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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.GhostPortrait;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.scene.Scene;
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

	private final IntroController sceneController = new IntroController();

	private Player2D pacMan2D;
	private List<Ghost2D> ghosts2D;
	private List<Ghost2D> gallery2D;

	public PacMan_IntroScene(PacManGameUI_JavaFX ui) {
		super(ui, ScenesPacMan.RENDERING, ScenesPacMan.SOUNDS);
	}

	@Override
	public void init(Scene parentScene, PacManGameController gameController) {
		super.init(parentScene, gameController);
		sceneController.init(gameController);

		score2D.showPoints = false;

		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);

		ghosts2D = Stream.of(sceneController.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, rendering);
			ghost2D.kickingAnimations.values().forEach(TimedSequence::restart);
			ghost2D.frightenedAnimation.restart();
			return ghost2D;
		}).collect(Collectors.toList());

		gallery2D = List.of( //
				new Ghost2D(sceneController.portraits[0].ghost, rendering),
				new Ghost2D(sceneController.portraits[1].ghost, rendering),
				new Ghost2D(sceneController.portraits[2].ghost, rendering),
				new Ghost2D(sceneController.portraits[3].ghost, rendering));
	}

	@Override
	public void doUpdate() {
		sceneController.updateState();
	}

	@Override
	public void doRender() {
		score2D.render(gc);
		highScore2D.render(gc);
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
			int offset = sceneController.stateTimer().ticked() % 5 < 2 ? 0 : -1;
			drawGuys(offset);
			break;
		case CHASING_GHOSTS:
			drawGallery();
			drawPoints(11, 25);
			drawCopyright(32);
			drawGuys(0);
			break;
		case READY_TO_PLAY:
			drawGallery();
			drawPressKeyToStart(24);
			break;
		default:
			break;
		}
	}

	private void drawGuys(int offset) {
		ghosts2D.get(0).render(gc);
		gc.save();
		gc.translate(offset, 0);
		ghosts2D.get(1).render(gc);
		ghosts2D.get(2).render(gc);
		gc.restore();
		ghosts2D.get(3).render(gc);
		pacMan2D.render(gc);
	}

	private void drawGallery() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("CHARACTER", t(6), sceneController.topY);
		gc.fillText("/", t(16), sceneController.topY);
		gc.fillText("NICKNAME", t(18), sceneController.topY);
		for (int ghostID = 0; ghostID < sceneController.portraits.length; ++ghostID) {
			GhostPortrait portrait = sceneController.portraits[ghostID];
			if (portrait.ghost.visible) {
				int y = sceneController.topY + t(1 + 3 * ghostID);
				gallery2D.get(ghostID).render(gc);
				if (portrait.characterVisible) {
					gc.setFill(rendering.getGhostColor(ghostID));
					gc.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					gc.setFill(rendering.getGhostColor(ghostID));
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
		gc.setFill(rendering.getFoodColor(1));
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
		gc.setFill(rendering.getFoodColor(1));
		gc.fillOval(t(2), t(20), TS, TS);
	}

	private void drawCopyright(int yTile) {
		String text = "\u00A9 1980 MIDWAY MFG. CO.";
		gc.setFont(rendering.getScoreFont());
		gc.setFill(rendering.getGhostColor(GameModel.PINK_GHOST));
		gc.fillText(text, t(3), t(yTile));
	}
}