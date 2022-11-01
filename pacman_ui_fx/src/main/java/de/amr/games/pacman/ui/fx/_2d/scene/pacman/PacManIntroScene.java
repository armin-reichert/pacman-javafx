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

import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
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
public class PacManIntroScene extends GameScene2D {

	private IntroController sceneController;
	private IntroController.Context icc;

	@Override
	public void setSceneContext(SceneContext sceneContext) {
		super.setSceneContext(sceneContext);
		sceneController = new IntroController(sceneContext.gameController());
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInState(IntroController.State.START);
		icc.pacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(icc.pacMan));
		Stream.of(icc.ghosts).forEach(ghost -> ghost.setAnimationSet(ctx.r2D().createGhostAnimationSet(ghost)));
	}

	@Override
	public void update() {
		sceneController.update();
		setCreditVisible(icc.creditVisible);
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
			ctx.game().intermissionTestNumber = 1;
			Actions.startCutscenesTest();
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var tick = sceneController.state().timer().tick();
		switch (sceneController.state()) {
		case START -> {
			drawGallery(g);
		}
		case PRESENTING_GHOSTS -> {
			drawGallery(g);
		}
		case SHOWING_POINTS -> {
			drawGallery(g);
			drawPoints(g);
			if (tick > TickTimer.secToTicks(1)) {
				drawBlinkingEnergizer(g);
				ctx.r2D().drawCopyright(g, 32);
			}
		}
		case CHASING_PAC -> {
			drawGallery(g);
			drawPoints(g);
			drawBlinkingEnergizer(g);
			drawGuys(g, flutter(tick));
			ctx.r2D().drawCopyright(g, 32);
		}
		case CHASING_GHOSTS -> {
			drawGallery(g);
			drawPoints(g);
			drawGuys(g, 0);
			ctx.r2D().drawCopyright(g, 32);
		}
		case READY_TO_PLAY -> {
			drawGallery(g);
			drawPoints(g);
			drawGuys(g, 0);
			ctx.r2D().drawCopyright(g, 32);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + sceneController.state());
		}
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter);
	}

	// TODO inspect in MAME what's really going on
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery(GraphicsContext g) {
		var font = ctx.r2D().arcadeFont();
		if (icc.titleVisible) {
			var color = Color.WHITE;
			ctx.r2D().drawText(g, "CHARACTER", color, font, t(icc.left + 3), t(6));
			ctx.r2D().drawText(g, "/", color, font, t(icc.left + 13), t(6));
			ctx.r2D().drawText(g, "NICKNAME", color, font, t(icc.left + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!icc.pictureVisible[id]) {
				continue;
			}
			int tileY = 7 + 3 * id;
			var color = ctx.r2D().ghostColor(id);
			ctx.r2D().drawSpriteCenteredOverBox(g, ctx.r2D().ghostSprite(id, Direction.RIGHT), t(icc.left) + 4, t(tileY));
			if (icc.characterVisible[id]) {
				ctx.r2D().drawText(g, "-" + icc.characters[id], color, font, t(icc.left + 3), t(tileY + 1));
			}
			if (icc.nicknameVisible[id]) {
				ctx.r2D().drawText(g, "\"" + icc.nicknames[id] + "\"", color, font, t(icc.left + 14), t(tileY + 1));
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if (Boolean.TRUE.equals(icc.blinking.frame())) {
			g.setFill(ctx.r2D().foodColor(1));
			g.fillOval(t(icc.left), t(20), TS, TS);
		}
	}

	private void drawGuys(GraphicsContext g, int offsetX) {
		if (offsetX == 0) {
			for (var ghost : icc.ghosts) {
				ctx.r2D().drawGhost(g, ghost);
			}
		} else {
			ctx.r2D().drawGhost(g, icc.ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			ctx.r2D().drawGhost(g, icc.ghosts[1]);
			ctx.r2D().drawGhost(g, icc.ghosts[2]);
			g.restore();
			ctx.r2D().drawGhost(g, icc.ghosts[3]);
		}
		ctx.r2D().drawPac(g, icc.pacMan);
	}

	private void drawPoints(GraphicsContext g) {
		int tileX = icc.left + 6;
		int tileY = 25;
		g.setFill(ctx.r2D().foodColor(1));
		g.fillRect(t(tileX) + 4.0, t(tileY - 1) + 4.0, 2, 2);
		if (Boolean.TRUE.equals(icc.blinking.frame())) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(Color.WHITE);
		g.setFont(ctx.r2D().arcadeFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(ctx.r2D().arcadeFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}