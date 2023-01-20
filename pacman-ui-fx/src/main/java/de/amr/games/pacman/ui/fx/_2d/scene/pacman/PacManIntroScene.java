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

import de.amr.games.pacman.controller.pacman.PacManIntroController;
import de.amr.games.pacman.controller.pacman.PacManIntroData;
import de.amr.games.pacman.controller.pacman.PacManIntroState;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
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

	private static final Color PALE_WHITE = Color.rgb(222, 222, 255);

	private PacManIntroController intro;

	@Override
	public void setContext(GameSceneContext sceneContext) {
		super.setContext(sceneContext);
		intro = new PacManIntroController(sceneContext.gameController());
	}

	@Override
	public void init() {
		intro.restart(PacManIntroState.START);
		intro.context().pacMan.setAnimations(ctx.r2D().createPacAnimations(intro.context().pacMan));
		Stream.of(intro.context().ghosts).forEach(ghost -> ghost.setAnimations(ctx.r2D().createGhostAnimations(ghost)));
		PacManIntroData.BLINKING.reset();
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
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter());
	}

	private void drawCopyright() {
		ctx.r2D().drawCopyright(g, 32);
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var r = ctx.r2D();
		var col = PacManIntroData.LEFT_TILE;
		var font = ctx.r2D().arcadeFont(TS);
		if (intro.context().titleVisible) {
			r.drawText(g, "CHARACTER", PALE_WHITE, font, t(col + 3), t(6));
			r.drawText(g, "/", PALE_WHITE, font, t(col + 13), t(6));
			r.drawText(g, "NICKNAME", PALE_WHITE, font, t(col + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!intro.context().pictureVisible[id]) {
				continue;
			}
			int tileY = 7 + 3 * id;
			var color = r.ghostColor(id);
			r.drawSpriteCenteredOverBox(g, r.ghostSprite(id, Direction.RIGHT), t(col) + 4, t(tileY));
			if (intro.context().characterVisible[id]) {
				r.drawText(g, "-" + PacManIntroData.CHARACTERS[id], color, font, t(col + 3), t(tileY + 1));
			}
			if (intro.context().nicknameVisible[id]) {
				r.drawText(g, quote(intro.context().ghosts[id].name()), color, font, t(col + 14), t(tileY + 1));
			}
		}
	}

	private static String quote(String s) {
		return "\"" + s + "\"";
	}

	private void drawBlinkingEnergizer() {
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.setFill(ctx.r2D().mazeFoodColor(1));
			g.fillOval(t(PacManIntroData.LEFT_TILE), t(20), TS, TS);
		}
	}

	private void drawGuys(int offsetX) {
		var pacMan = intro.context().pacMan;
		var ghosts = intro.context().ghosts;
		if (offsetX == 0) {
			for (var ghost : ghosts) {
				ctx.r2D().drawGhost(g, ghost);
			}
		} else {
			ctx.r2D().drawGhost(g, ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			ctx.r2D().drawGhost(g, ghosts[1]);
			ctx.r2D().drawGhost(g, ghosts[2]);
			g.restore();
			ctx.r2D().drawGhost(g, ghosts[3]);
		}
		ctx.r2D().drawPac(g, pacMan);
	}

	private void drawPoints() {
		int tileX = PacManIntroData.LEFT_TILE + 6;
		int tileY = 25;
		g.setFill(ctx.r2D().mazeFoodColor(1));
		g.fillRect(t(tileX) + 4.0, t(tileY - 1) + 4.0, 2, 2);
		if (Boolean.TRUE.equals(PacManIntroData.BLINKING.frame())) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(PALE_WHITE);
		g.setFont(ctx.r2D().arcadeFont(TS));
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(ctx.r2D().arcadeFont(6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}