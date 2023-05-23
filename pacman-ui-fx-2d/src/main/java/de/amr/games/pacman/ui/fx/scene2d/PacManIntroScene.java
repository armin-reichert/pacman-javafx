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
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.controller.PacManIntro;
import de.amr.games.pacman.controller.PacManIntro.State;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

	private static final String QUOTE = "\"";

	private PacManGameRenderer r;
	private PacManIntro intro;
	private PacManIntro.Context ic;
	private Signature signature;

	public PacManIntroScene() {
		signature = new Signature();
		overlay.getChildren().add(signature.root());
	}

	@Override
	public void init() {
		r = context.rendererPacMan();

		context.setCreditVisible(true);
		context.setScoreVisible(true);

		signature.setNameFont(r.theme().font("font.handwriting", 9));
		signature.hide();

		intro = new PacManIntro(context().gameController());
		intro.addStateChangeListener((oldState, newState) -> {
			if (oldState == PacManIntro.State.SHOWING_POINTS) {
				signature.show(t(5.5), t(32.0));
			}
		});
		ic = intro.context();

		ic.pacMan.setAnimations(r.createPacAnimations(ic.pacMan));
		ic.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
		ic.blinking.reset();

		intro.changeState(State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		context.ui().stopVoice();
		signature.hide();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			context.ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			context.ui().startGame();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			context.ui().selectNextGameVariant();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			context.ui().startCutscenesTest();
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var timer = intro.state().timer();
		drawGallery(g);
		switch (intro.state()) {
		case SHOWING_POINTS -> {
			drawPoints(g);
		}
		case CHASING_PAC -> {
			drawPoints(g);
			drawBlinkingEnergizer(g);
			drawGuys(g, flutter(timer.tick()));
			r.drawMidwayCopyright(g, t(4), t(32));
		}
		case CHASING_GHOSTS -> {
			drawPoints(g);
			drawGuys(g, 0);
			r.drawMidwayCopyright(g, t(4), t(32));
		}
		case READY_TO_PLAY -> {
			drawPoints(g);
			drawGuys(g, 0);
			r.drawMidwayCopyright(g, t(4), t(32));
		}
		default -> {
			// nothing to do
		}
		}
		r.drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
	}

	@Override
	protected void drawSceneInfo(GraphicsContext g) {
		GameRenderer.drawTileGrid(g, TILES_X, TILES_Y);
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery(GraphicsContext g) {
		var theme = context.renderer().theme();
		var font8 = theme.font("font.arcade", 8);

		int tx = ic.leftTileX;
		if (ic.titleVisible) {
			GameRenderer.drawText(g, "CHARACTER / NICKNAME", ArcadeTheme.PALE, font8, t(tx + 3), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!ic.ghostInfo[id].pictureVisible) {
				continue;
			}
			int ty = 7 + 3 * id;
			r.drawGhostFacingRight(g, id, t(tx) + 4, t(ty));
			if (ic.ghostInfo[id].characterVisible) {
				var text = "-" + ic.ghostInfo[id].character;
				var color = theme.color("ghost.%d.color.normal.dress".formatted(id));
				GameRenderer.drawText(g, text, color, font8, t(tx + 3), t(ty + 1));
			}
			if (ic.ghostInfo[id].nicknameVisible) {
				var text = QUOTE + ic.ghostInfo[id].ghost.name() + QUOTE;
				var color = theme.color("ghost.%d.color.normal.dress".formatted(id));
				GameRenderer.drawText(g, text, color, font8, t(tx + 14), t(ty + 1));
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		var theme = context.ui().assets().arcadeTheme; // TODO
		if (Boolean.TRUE.equals(ic.blinking.frame())) {
			g.setFill(theme.color("pacman.maze.foodColor"));
			g.fillOval(t(ic.leftTileX), t(20), TS, TS);
		}
	}

	private void drawGuys(GraphicsContext g, int shakingAmount) {
		if (shakingAmount == 0) {
			ic.ghosts().forEach(ghost -> r.drawGhost(g, ghost));
		} else {
			r.drawGhost(g, ic.ghost(0));
			r.drawGhost(g, ic.ghost(3));
			// shaking ghosts effect, not quite as in original game
			g.save();
			g.translate(shakingAmount, 0);
			r.drawGhost(g, ic.ghost(1));
			r.drawGhost(g, ic.ghost(2));
			g.restore();
		}
		r.drawPac(g, ic.pacMan);
	}

	private void drawPoints(GraphicsContext g) {
		var theme = context.renderer().theme();
		var font6 = theme.font("font.arcade", 6);
		var font8 = theme.font("font.arcade", 8);

		int tx = ic.leftTileX + 6;
		int ty = 25;
		g.setFill(theme.color("pacman.maze.foodColor"));
		g.fillRect(t(tx) + 4, t(ty - 1) + 4, 2, 2);
		if (Boolean.TRUE.equals(ic.blinking.frame())) {
			g.fillOval(t(tx), t(ty + 1), TS, TS);
		}
		g.setFill(ArcadeTheme.PALE);
		g.setFont(font8);
		g.fillText("10", t(tx + 2), t(ty));
		g.setFont(font6); // TODO looks ugly
		g.fillText("PTS", t(tx + 5), t(ty));
		g.setFont(font8);
		g.fillText("50", t(tx + 2), t(ty + 2));
		g.setFont(font6); // TODO still looks ugly
		g.fillText("PTS", t(tx + 5), t(ty + 2));
	}
}