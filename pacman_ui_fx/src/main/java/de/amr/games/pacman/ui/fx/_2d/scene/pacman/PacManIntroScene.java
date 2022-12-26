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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.pacman.PacManIntroData;
import de.amr.games.pacman.controller.pacman.PacManIntroState;
import de.amr.games.pacman.controller.pacman.PacManIntroController;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

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

	@Override
	public void setContext(SceneContext sceneContext) {
		super.setContext(sceneContext);
		intro = new PacManIntroController(sceneContext.gameController());
	}

	@Override
	public void init() {
		intro.restart(PacManIntroState.START);
		intro.context().pacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(intro.context().pacMan));
		Stream.of(intro.context().ghosts).forEach(ghost -> ghost.setAnimationSet(ctx.r2D().createGhostAnimationSet(ghost)));
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
		var timer = intro.state().timer();

		switch (intro.state()) {
		case START -> {
			drawGallery();
		}
		case PRESENTING_GHOSTS -> {
			drawGallery();
		}
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
		default -> throw new IllegalArgumentException("Unexpected value: " + intro.state());
		}
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter());
	}

	private void drawCopyright() {
		ctx.r2D().drawCopyright(g, 32);
	}

	// TODO inspect in MAME what's really going on
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var font = ctx.r2D().arcadeFont(TS);
		if (intro.context().titleVisible) {
			var color = Color.rgb(222, 222, 255);
			ctx.r2D().drawText(g, "CHARACTER", color, font, t(PacManIntroData.LEFT_TILE + 3), t(6));
			ctx.r2D().drawText(g, "/", color, font, t(PacManIntroData.LEFT_TILE + 13), t(6));
			ctx.r2D().drawText(g, "NICKNAME", color, font, t(PacManIntroData.LEFT_TILE + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!intro.context().pictureVisible[id]) {
				continue;
			}
			int tileY = 7 + 3 * id;
			var color = ctx.r2D().ghostColor(id);
			ctx.r2D().drawSpriteCenteredOverBox(g, ctx.r2D().ghostSprite(id, Direction.RIGHT), t(PacManIntroData.LEFT_TILE) + 4,
					t(tileY));
			if (intro.context().characterVisible[id]) {
				ctx.r2D().drawText(g, "-" + PacManIntroData.CHARACTERS[id], color, font, t(PacManIntroData.LEFT_TILE + 3), t(tileY + 1));
			}
			if (intro.context().nicknameVisible[id]) {
				ctx.r2D().drawText(g, "\"" + PacManIntroData.NICKNAMES[id] + "\"", color, font, t(PacManIntroData.LEFT_TILE + 14),
						t(tileY + 1));
			}
		}
	}

	private void drawBlinkingEnergizer() {
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.setFill(ctx.r2D().getMazeFoodColor(1));
			g.fillOval(t(PacManIntroData.LEFT_TILE), t(20), TS, TS);
		}
	}

	private void drawGuys(int offsetX) {
		if (offsetX == 0) {
			for (var ghost : intro.context().ghosts) {
				ctx.r2D().drawGhost(g, ghost);
			}
		} else {
			ctx.r2D().drawGhost(g, intro.context().ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			ctx.r2D().drawGhost(g, intro.context().ghosts[1]);
			ctx.r2D().drawGhost(g, intro.context().ghosts[2]);
			g.restore();
			ctx.r2D().drawGhost(g, intro.context().ghosts[3]);
		}
		ctx.r2D().drawPac(g, intro.context().pacMan);
	}

	private void drawPoints() {
		int tileX = PacManIntroData.LEFT_TILE + 6;
		int tileY = 25;
		g.setFill(ctx.r2D().getMazeFoodColor(1));
		g.fillRect(t(tileX) + 4.0, t(tileY - 1) + 4.0, 2, 2);
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(Color.rgb(222, 222, 255));
		g.setFont(ctx.r2D().arcadeFont(TS));
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(ctx.r2D().arcadeFont(6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}