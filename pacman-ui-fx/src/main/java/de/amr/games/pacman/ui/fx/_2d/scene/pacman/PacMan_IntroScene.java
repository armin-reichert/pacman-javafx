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
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Credit2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

	private final IntroController sceneController;
	private final IntroController.Context context;

	private Player2D pacMan2D;
	private Ghost2D[] ghosts2D;
	private Credit2D credit2D;

	public PacMan_IntroScene(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new IntroController(gameController);
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.BEGIN);
		createScores();
		score2D.showPoints = false;
		credit2D = new Credit2D(gameController::credit);
		pacMan2D = new Player2D(context.pacMan, game).createAnimations(r2D);
		pacMan2D.animMunching.values().forEach(SpriteAnimation::restart);

		ghosts2D = Stream.of(context.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, game).createAnimations(r2D);
			ghost2D.animKicking.values().forEach(SpriteAnimation::restart);
			ghost2D.animFrightened.restart();
			return ghost2D;
		}).toArray(Ghost2D[]::new);
	}

	@Override
	public void handleKeyPressed(KeyEvent e) {
		if (GameUI.pressed(e, KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		} else if (GameUI.pressed(e, KeyCode.SPACE) || GameUI.pressed(e, KeyCode.DIGIT1)) {
			gameController.requestGame();
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		// TODO find a better solution:
		if (sceneController.state() == IntroController.State.CHASING_GHOSTS) {
			for (Ghost ghost : context.ghosts) {
				if (ghost.velocity.equals(V2d.NULL)) {
					ghosts2D[ghost.id].animFrightened.stop();
				} else if (!ghosts2D[ghost.id].animFrightened.isRunning()) {
					ghosts2D[ghost.id].animFrightened.restart();
				}
			}
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		score2D.render(g, r2D);
		highScore2D.render(g, r2D);
		credit2D.render(g, r2D);
		switch (sceneController.state()) {
		case BEGIN, PRESENTING_GHOSTS -> drawGallery();
		case SHOWING_POINTS -> {
			drawGallery();
			drawPoints(11, 25);
			if (sceneController.state().timer().tick() > sec_to_ticks(1)) {
				drawEnergizer();
				r2D.renderCopyright(g, t(3), t(32));
			}
		}
		case CHASING_PAC -> {
			drawGallery();
			drawPoints(11, 25);
			r2D.renderCopyright(g, t(3), t(32));
			if (context.fastBlinking.frame()) {
				drawEnergizer();
			}
			int offset = sceneController.state().timer().tick() % 5 < 2 ? 0 : -1;
			drawGuys(offset);
		}
		case CHASING_GHOSTS -> {
			drawGallery();
			drawPoints(11, 25);
			r2D.renderCopyright(g, t(3), t(32));
			drawGuys(0);
		}
		case READY_TO_PLAY -> {
			drawGallery();
		}
		default -> {
		}
		}
	}

	private void drawGuys(int offset) {
		var g = canvas.getGraphicsContext2D();
		ghosts2D[0].render(g, r2D);
		g.save();
		g.translate(offset, 0);
		ghosts2D[1].render(g, r2D);
		ghosts2D[2].render(g, r2D);
		g.restore();
		ghosts2D[3].render(g, r2D);
		pacMan2D.render(g, r2D);
	}

	private void drawGallery() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("CHARACTER", t(6), t(6));
		g.fillText("/", t(16), t(6));
		g.fillText("NICKNAME", t(18), t(6));
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			if (context.pictureVisible[ghostID]) {
				int tileY = 7 + 3 * ghostID;
				drawGhost(g, ghostID, t(3), t(tileY));
				if (context.characterVisible[ghostID]) {
					g.setFill(r2D.getGhostSkinColor(ghostID));
					g.fillText("-" + context.characters[ghostID], t(6), t(tileY + 1));
				}
				if (context.nicknameVisible[ghostID]) {
					g.setFill(r2D.getGhostSkinColor(ghostID));
					g.fillText("\"" + context.nicknames[ghostID] + "\"", t(17), t(tileY + 1));
				}
			}
		}
	}

	private void drawGhost(GraphicsContext g, int ghostID, int x, int y) {
		Rectangle2D sprite = Rendering2D_PacMan.get().spritesheet().r(0, 4 + ghostID);
		Rendering2D_PacMan.get().renderSprite(g, sprite, x + 4 - sprite.getWidth() / 2, y + 4 - sprite.getHeight() / 2);
	}

	private void drawPoints(int tileX, int tileY) {
		var g = canvas.getGraphicsContext2D();
		g.setFill(r2D.getFoodColor(1));
		g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
		if (context.fastBlinking.frame()) {
			g.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(r2D.getArcadeFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}

	private void drawEnergizer() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(r2D.getFoodColor(1));
		g.fillOval(t(3), t(20), TS, TS);
	}
}