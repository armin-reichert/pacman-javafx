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

import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
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
		sceneController = new IntroController(sceneContext.gameController);
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(IntroController.State.WARMUP);
		creditVisible = false;
		icc.pacMan.setAnimationSet(new PacAnimations(icc.pacMan, ctx.r2D));
		Stream.of(icc.ghosts).forEach(ghost -> ghost.setAnimationSet(new GhostAnimations(ghost, ctx.r2D)));
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			ctx.state().addCredit(ctx.game());
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			Actions.startGame();
		} else if (Keyboard.pressed(KeyCode.V)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.Z)) {
			ctx.game().intermissionTestNumber = 1;
			ctx.state().startIntermissionTest(ctx.game());
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		creditVisible = icc.creditVisible;
	}

	private boolean between(double secLeft, double secRight, double tick) {
		return TickTimer.secToTicks(secLeft) <= tick && tick < TickTimer.secToTicks(secRight);
	}

	@Override
	public void doRender(GraphicsContext g) {
		var tick = sceneController.state().timer().tick();
		switch (sceneController.state()) {
		case WARMUP -> {
			if (between(1.0, 2.0, tick)) {
				drawHexCodes(g, tick);
			} else if (between(2.0, 3.0, tick)) {
				drawRandomSprites(g, tick);
			} else if (between(3.0, 4.0, tick)) {
				drawGrid(g);
			}
		}
		case START -> {
			drawScoresAndCredit(g);
			drawGallery(g);
		}
		case PRESENTING_GHOSTS -> {
			drawScoresAndCredit(g);
			drawGallery(g);
		}
		case SHOWING_POINTS -> {
			drawScoresAndCredit(g);
			drawGallery(g);
			drawPoints(g);
			if (tick > TickTimer.secToTicks(1)) {
				drawBlinkingEnergizer(g);
				ctx.r2D.drawCopyright(g, 32);
			}
		}
		case CHASING_PAC -> {
			drawScoresAndCredit(g);
			drawGallery(g);
			drawPoints(g);
			drawBlinkingEnergizer(g);
			drawGuys(g, flutter(tick));
			ctx.r2D.drawCopyright(g, 32);
		}
		case CHASING_GHOSTS -> {
			drawScoresAndCredit(g);
			drawGallery(g);
			drawPoints(g);
			drawGuys(g, 0);
			ctx.r2D.drawCopyright(g, 32);
		}
		case READY_TO_PLAY -> {
			drawScoresAndCredit(g);
			drawGallery(g);
			drawPoints(g);
			drawGuys(g, 0);
			ctx.r2D.drawCopyright(g, 32);
		}
		}
	}

	private void drawScoresAndCredit(GraphicsContext g) {
		ctx.r2D.drawScore(g, ctx.game().scores.gameScore);
		ctx.r2D.drawScore(g, ctx.game().scores.highScore);
		if (creditVisible) {
			ctx.r2D.drawCredit(g, ctx.game().credit);
		}
	}

	// TODO inspect in MAME what's really going on
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private Random rnd = new Random();

	private void drawHexCodes(GraphicsContext g, long tick) {
		g.setFill(Color.LIGHTGRAY);
		g.setFont(SpritesheetPacMan.get().getArcadeFont());
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
				var hexCode = Integer.toHexString(rnd.nextInt(16));
				g.fillText(hexCode, col * 8, row * 8 + 8);
			}
		}
	}

	private void drawRandomSprites(GraphicsContext g, long tick) {
		for (int row = 0; row < ArcadeWorld.TILES_Y / 2; ++row) {
			for (int col = 0; col < ArcadeWorld.TILES_X / 2; ++col) {
				var x = rnd.nextInt(14);
				var y = rnd.nextInt(10);
				var sprite = SpritesheetPacMan.get().subImage(x * 16, y * 16 + 8, 16, 16);
				g.drawImage(sprite, col * 2 * TS, row * 2 * TS);
			}
		}
	}

	private void drawGrid(GraphicsContext g) {
		g.setStroke(Color.LIGHTGRAY);
		g.setLineWidth(2.0);
		for (int row = 0; row < ArcadeWorld.TILES_Y / 2; ++row) {
			g.strokeLine(0, row * 2 * TS, ArcadeWorld.TILES_X * TS, row * 2 * TS);
		}
		for (int col = 0; col < ArcadeWorld.TILES_X / 2; ++col) {
			g.strokeLine(col * 2 * TS, 0, col * 2 * TS, ArcadeWorld.TILES_Y * TS);
		}
	}

	private void drawGallery(GraphicsContext g) {
		if (icc.titleVisible) {
			g.setFill(Color.WHITE);
			g.setFont(ctx.r2D.getArcadeFont());
			g.fillText("CHARACTER", t(icc.left + 3), t(6));
			g.fillText("/", t(icc.left + 13), t(6));
			g.fillText("NICKNAME", t(icc.left + 15), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (icc.pictureVisible[id]) {
				int tileY = 7 + 3 * id;
				ctx.r2D.drawSpriteCenteredOverBox(g, ctx.r2D.getGhostSprite(id, Direction.RIGHT), t(icc.left) + 4, t(tileY));
				if (icc.characterVisible[id]) {
					g.setFill(ctx.r2D.getGhostColor(id));
					g.setFont(ctx.r2D.getArcadeFont());
					g.fillText("-" + icc.characters[id], t(icc.left + 3), t(tileY + 1));
				}
				if (icc.nicknameVisible[id]) {
					g.setFill(ctx.r2D.getGhostColor(id));
					g.setFont(ctx.r2D.getArcadeFont());
					g.fillText("\"" + icc.nicknames[id] + "\"", t(icc.left + 14), t(tileY + 1));
				}
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if (Boolean.TRUE.equals(icc.blinking.frame())) {
			g.setFill(ctx.r2D.getFoodColor(1));
			g.fillOval(t(icc.left), t(20), TS, TS);
		}
	}

	private void drawGuys(GraphicsContext g, int offsetX) {
		if (offsetX == 0) {
			ctx.r2D.drawGhosts(g, icc.ghosts);
		} else {
			ctx.r2D.drawGhost(g, icc.ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			ctx.r2D.drawGhost(g, icc.ghosts[1]);
			ctx.r2D.drawGhost(g, icc.ghosts[2]);
			g.restore();
			ctx.r2D.drawGhost(g, icc.ghosts[3]);
		}
		ctx.r2D.drawPac(g, icc.pacMan);
	}

	private void drawPoints(GraphicsContext g) {
		int tileX = icc.left + 6;
		int tileY = 25;
		g.setFill(ctx.r2D.getFoodColor(1));
		g.fillRect(t(tileX) + 4.0, t(tileY - 1) + 4.0, 2, 2);
		if (Boolean.TRUE.equals(icc.blinking.frame())) {
			g.fillOval(t(tileX), t(tileY + 1), TS, TS);
		}
		g.setFill(Color.WHITE);
		g.setFont(ctx.r2D.getArcadeFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(ctx.r2D.getArcadeFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}