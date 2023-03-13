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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.PacManIntroController;
import de.amr.games.pacman.controller.pacman.PacManIntroData;
import de.amr.games.pacman.controller.pacman.PacManIntroState;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

	private PacManIntroController intro;

	public PacManIntroScene(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		intro = new PacManIntroController(context().gameController());
		intro.restart(PacManIntroState.START);
		intro.context().pacMan.setAnimations(context.r2D().createPacAnimations(intro.context().pacMan));
		Stream.of(intro.context().ghosts).forEach(ghost -> ghost.setAnimations(context.r2D().createGhostAnimations(ghost)));
		PacManIntroData.BLINKING.reset();
	}

	@Override
	public void update() {
		intro.update();
		context.setCreditVisible(intro.context().creditVisible);
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
	public void drawSceneContent() {
		var timer = intro.state().timer();
		switch (intro.state()) {
		case START -> drawGallery();
		case PRESENTING_GHOSTS -> drawGallery();
		case SHOWING_POINTS -> {
			drawGallery();
			drawPoints();
			if (timer.tick() > timer.secToTicks(1)) {
				drawBlinkingEnergizer();
				drawCopyright();
			}
		}
		case CHASING_PAC -> {
			drawGallery();
			drawPoints();
			drawBlinkingEnergizer();
			drawGuys(flutter(timer.tick()));
			drawCopyright();
		}
		case CHASING_GHOSTS -> {
			drawGallery();
			drawPoints();
			drawGuys(0);
			drawCopyright();
		}
		case READY_TO_PLAY -> {
			drawGallery();
			drawPoints();
			drawGuys(0);
			drawCopyright();
		}
		default -> throw new IllegalArgumentException("Unknown intro state: " + intro.state());
		}
		context.r2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	private void drawCopyright() {
		context.r2D().drawText(g, "\u00A9 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, ArcadeTheme.SCREEN_FONT, t(4), t(32));
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var r = context.r2D();
		var col = PacManIntroData.LEFT_TILE;
		var font = context.r2D().screenFont(TS);
		if (intro.context().titleVisible) {
			r.drawText(g, "CHARACTER", ArcadeTheme.PALE, font, t(col + 3), t(6));
			r.drawText(g, "/", ArcadeTheme.PALE, font, t(col + 13), t(6));
			r.drawText(g, "NICKNAME", ArcadeTheme.PALE, font, t(col + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!intro.context().pictureVisible[id]) {
				continue;
			}
			int row = 7 + 3 * id;
			var color = r.ghostColoring(id).normalDress();
			r.drawGhostFacingRight(g, id, t(col) + 4, t(row));
			if (intro.context().characterVisible[id]) {
				r.drawText(g, "-" + PacManIntroData.CHARACTERS[id], color, font, t(col + 3), t(row + 1));
			}
			if (intro.context().nicknameVisible[id]) {
				r.drawText(g, quote(intro.context().ghosts[id].name()), color, font, t(col + 14), t(row + 1));
			}
		}
	}

	private static String quote(String s) {
		return "\"" + s + "\"";
	}

	private void drawBlinkingEnergizer() {
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.setFill(context.r2D().mazeColoring(1).foodColor());
			g.fillOval(t(PacManIntroData.LEFT_TILE), t(20), TS, TS);
		}
	}

	private void drawGuys(int offsetX) {
		var r = context.r2D();
		var pacMan = intro.context().pacMan;
		var ghosts = intro.context().ghosts;
		if (offsetX == 0) {
			for (var ghost : ghosts) {
				r.drawGhost(g, ghost);
			}
		} else {
			r.drawGhost(g, ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			r.drawGhost(g, ghosts[1]);
			r.drawGhost(g, ghosts[2]);
			g.restore();
			r.drawGhost(g, ghosts[3]);
		}
		r.drawPac(g, pacMan);
	}

	private void drawPoints() {
		var r = context.r2D();
		int col = PacManIntroData.LEFT_TILE + 6;
		int row = 25;
		g.setFill(r.mazeColoring(1).foodColor());
		g.fillRect(t(col) + 4, t(row - 1) + 4, 2, 2);
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.fillOval(t(col), t(row + 1), TS, TS);
		}
		g.setFill(ArcadeTheme.PALE);
		g.setFont(r.screenFont(TS));
		g.fillText("10", t(col + 2), t(row));
		g.fillText("50", t(col + 2), t(row + 2));
		g.setFont(r.screenFont(6));
		g.fillText("PTS", t(col + 5), t(row));
		g.fillText("PTS", t(col + 5), t(row + 2));
	}
}