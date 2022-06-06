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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.GhostAnimation;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimationSet;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Key;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
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
public class PacMan_IntroScene extends GameScene2D {

	private IntroController sceneController;
	private IntroController.Context $;
	private Pac2D pacMan2D;
	private Ghost2D[] ghosts2D;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new IntroController(gameController);
		sceneController.addStateChangeListener(this::onSceneStateChange);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.START);
		createCommonParts(game);
		score2D.showScore = false;
		credit2D.visible = true;
		pacMan2D = new Pac2D($.pacMan, new PacAnimationSet(r2D));
		ghosts2D = Stream.of($.ghosts)//
				.map(ghost -> new Ghost2D(ghost, new GhostAnimationSet(ghost.id, r2D)))//
				.toArray(Ghost2D[]::new);
	}

	@Override
	public void onKeyPressed() {
		if (Key.pressed(KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		} else if (Key.pressed(KeyCode.DIGIT1)) {
			gameController.requestGame();
		} else if (Key.pressed(Key.ALT, KeyCode.Z)) {
			Actions.startIntermissionScenesTest();
		}
	}

	private void onSceneStateChange(State fromState, State toState) {
		if (fromState == State.CHASING_PAC && toState == State.CHASING_GHOSTS) {
			for (var ghost2D : ghosts2D) {
				ghost2D.animations.selectAnimation(GhostAnimation.BLUE);
			}
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		updateAnimations();
	}

	private void updateAnimations() {
		// TODO this is not elegant but works
		if (sceneController.state() == State.CHASING_GHOSTS) {
			for (var ghost2D : ghosts2D) {
				if (ghost2D.ghost.bounty > 0) {
					ghost2D.animations.selectAnimation(GhostAnimation.NUMBER);
				} else {
					ghost2D.animations.selectAnimation(GhostAnimation.BLUE);
					if (ghost2D.ghost.velocity.length() == 0) {
						ghost2D.animations.stop(GhostAnimation.BLUE);
					} else {
						ghost2D.animations.run(GhostAnimation.BLUE);
					}
				}
			}
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		var time = sceneController.state().timer().tick();

		score2D.render(g, r2D);
		highScore2D.render(g, r2D);
		credit2D.render(g, r2D);

		switch (sceneController.state()) {
		case START, PRESENTING_GHOSTS -> drawGallery(g);
		case SHOWING_POINTS -> {
			drawGallery(g);
			drawPoints(g);
			if (time > sec_to_ticks(1)) {
				drawBlinkingEnergizer(g);
				r2D.drawCopyright(g);
			}
		}
		case CHASING_PAC -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g);
			drawBlinkingEnergizer(g);
			drawGuys(g, time % 5 < 2 ? 0 : -1); // TODO check this
		}
		case CHASING_GHOSTS -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g);
			drawGuys(g, 0);
		}
		case READY_TO_PLAY -> {
			drawGallery(g);
			drawPoints(g);
			r2D.drawCopyright(g);
			drawGuys(g, 0);
		}
		}
	}

	private void drawGallery(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("CHARACTER", t($.left + 3), t(6));
		g.fillText("/", t($.left + 13), t(6));
		g.fillText("NICKNAME", t($.left + 15), t(6));
		for (int id = 0; id < 4; ++id) {
			if ($.pictureVisible[id]) {
				int tileY = 7 + 3 * id;
				r2D.drawSpriteCenteredOverBox(g, r2D.getGhostSprite(id, Direction.RIGHT), t($.left) + 4, t(tileY));
				if ($.characterVisible[id]) {
					g.setFill(r2D.getGhostColor(id));
					g.fillText("-" + $.characters[id], t($.left + 3), t(tileY + 1));
				}
				if ($.nicknameVisible[id]) {
					g.setFill(r2D.getGhostColor(id));
					g.fillText("\"" + $.nicknames[id] + "\"", t($.left + 14), t(tileY + 1));
				}
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if ($.blinking.frame()) {
			g.setFill(r2D.getFoodColor(1));
			g.fillOval(t($.left), t(20), TS, TS);
		}
	}

	// TODO check this
	private void drawGuys(GraphicsContext g, int offset) {
		pacMan2D.render(g, r2D);
		if (offset == 0) {
			ghosts2D[0].render(g, r2D);
			ghosts2D[1].render(g, r2D);
			ghosts2D[2].render(g, r2D);
			ghosts2D[3].render(g, r2D);
		} else {
			ghosts2D[0].render(g, r2D);
			g.save();
			g.translate(offset, 0);
			ghosts2D[1].render(g, r2D);
			ghosts2D[2].render(g, r2D);
			g.restore();
			ghosts2D[3].render(g, r2D);
		}
	}

	private void drawPoints(GraphicsContext g) {
		int tileX = $.left + 6, tileY = 25;
		g.setFill(r2D.getFoodColor(1));
		g.fillRect(t(tileX) + 4, t(tileY - 1) + 4, 2, 2);
		if ($.blinking.frame()) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(r2D.getArcadeFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}