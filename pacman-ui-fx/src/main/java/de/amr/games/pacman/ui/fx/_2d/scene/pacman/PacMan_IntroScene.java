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
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.GhostPortrait;
import de.amr.games.pacman.controller.pacman.IntroController.IntroState;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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

	private final IntroController sc;

	private Player2D pacMan2D;
	private Ghost2D[] ghosts2D;
	private Ghost2D[] gallery2D;

	public PacMan_IntroScene(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sc = new IntroController(gameController);
	}

	@Override
	public void init() {
		super.init();
		sc.init();

		score2D.showPoints = false;

		pacMan2D = new Player2D(sc.pacMan, game, r2D);
		pacMan2D.animMunching.values().forEach(TimedSeq::restart);

		ghosts2D = Stream.of(sc.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, game, r2D);
			ghost2D.animKicking.values().forEach(TimedSeq::restart);
			ghost2D.animFrightened.restart();
			return ghost2D;
		}).toArray(Ghost2D[]::new);

		gallery2D = new Ghost2D[] { //
				new Ghost2D(sc.portraits[0].ghost, game, r2D), //
				new Ghost2D(sc.portraits[1].ghost, game, r2D), //
				new Ghost2D(sc.portraits[2].ghost, game, r2D), //
				new Ghost2D(sc.portraits[3].ghost, game, r2D) };
	}

	@Override
	public void doUpdate() {
		sc.updateState();
		// TODO find a better solution:
		if (sc.state == IntroState.CHASING_GHOSTS) {
			for (Ghost ghost : sc.ghosts) {
				if (ghost.velocity.equals(V2d.NULL)) {
					ghosts2D[ghost.id].animFrightened.stop();
				} else if (!ghosts2D[ghost.id].animFrightened.isRunning()) {
					ghosts2D[ghost.id].animFrightened.restart();
				}
			}
		}
	}

	@Override
	public void doRender() {
		var g = canvas.getGraphicsContext2D();
		score2D.render(g);
		highScore2D.render(g);

		switch (sc.state) {

		case BEGIN, PRESENTING_GHOSTS -> drawGallery();

		case SHOWING_POINTS -> {
			drawGallery();
			drawPoints(11, 25);
			if (sc.stateTimer().ticked() > sec_to_ticks(1)) {
				drawEnergizer();
				drawCopyright(32);
			}
		}

		case CHASING_PAC -> {
			drawGallery();
			drawPoints(11, 25);
			drawCopyright(32);
			if (sc.fastBlinking.frame()) {
				drawEnergizer();
			}
			int offset = sc.stateTimer().ticked() % 5 < 2 ? 0 : -1;
			drawGuys(offset);
		}

		case CHASING_GHOSTS -> {
			drawGallery();
			drawPoints(11, 25);
			drawCopyright(32);
			drawGuys(0);
		}

		case READY_TO_PLAY -> {
			drawGallery();
			drawPressKeyToStart(24);
		}

		default -> {
		}

		}
	}

	private void drawGuys(int offset) {
		var g = canvas.getGraphicsContext2D();
		ghosts2D[0].render(g);
		g.save();
		g.translate(offset, 0);
		ghosts2D[1].render(g);
		ghosts2D[2].render(g);
		g.restore();
		ghosts2D[3].render(g);
		pacMan2D.render(g);
	}

	private void drawGallery() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("CHARACTER", t(6), sc.topY);
		g.fillText("/", t(16), sc.topY);
		g.fillText("NICKNAME", t(18), sc.topY);
		for (int ghostID = 0; ghostID < sc.portraits.length; ++ghostID) {
			GhostPortrait portrait = sc.portraits[ghostID];
			if (portrait.ghost.visible) {
				int y = sc.topY + t(1 + 3 * ghostID);
				gallery2D[ghostID].render(g);
				if (portrait.characterVisible) {
					g.setFill(r2D.getGhostColor(ghostID));
					g.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					g.setFill(r2D.getGhostColor(ghostID));
					g.fillText("\"" + portrait.ghost.name + "\"", t(17), y + 8);
				}
			}
		}
	}

	private void drawPressKeyToStart(int yTile) {
		var g = canvas.getGraphicsContext2D();
		if (sc.slowBlinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.WHITE);
			g.setFont(r2D.getArcadeFont());
			g.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPoints(int tileX, int tileY) {
		var g = canvas.getGraphicsContext2D();
		g.setFill(r2D.getFoodColor(1));
		g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
		if (sc.fastBlinking.frame()) {
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
		g.fillOval(t(2), t(20), TS, TS);
	}

	private void drawCopyright(int yTile) {
		var g = canvas.getGraphicsContext2D();
		String text = "\u00A9 1980 MIDWAY MFG. CO.";
		g.setFont(r2D.getArcadeFont());
		g.setFill(r2D.getGhostColor(GameModel.PINK_GHOST));
		g.fillText(text, t(3), t(yTile));
	}
}