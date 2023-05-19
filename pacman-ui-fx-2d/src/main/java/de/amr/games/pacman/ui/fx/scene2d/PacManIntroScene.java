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
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
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
	private final Signature signature = new Signature();

	public PacManIntroScene() {
		overlay.getChildren().add(signature.root());
	}

	@Override
	public void init() {
		r = (PacManGameRenderer) context.rendering2D();

		context.setCreditVisible(true);
		context.setScoreVisible(true);

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
		Game2d.ui.stopVoice();
		signature.hide();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Game2d.KEY_ADD_CREDIT) || Keyboard.pressed(Game2d.KEY_ADD_CREDIT_NUMPAD)) {
			Game2d.app.addCredit();
		} else if (Keyboard.pressed(Game2d.KEY_START_GAME) || Keyboard.pressed(Game2d.KEY_START_GAME_NUMPAD)) {
			Game2d.app.startGame();
		} else if (Keyboard.pressed(Game2d.KEY_SELECT_VARIANT)) {
			Game2d.app.selectNextGameVariant();
		} else if (Keyboard.pressed(Game2d.KEY_PLAY_CUTSCENES)) {
			Game2d.app.startCutscenesTest();
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
			PacManGameRenderer.drawMidwayCopyright(g, 4, 32);
		}
		case CHASING_GHOSTS -> {
			drawPoints(g);
			drawGuys(g, 0);
			PacManGameRenderer.drawMidwayCopyright(g, 4, 32);
		}
		case READY_TO_PLAY -> {
			drawPoints(g);
			drawGuys(g, 0);
			PacManGameRenderer.drawMidwayCopyright(g, 4, 32);
		}
		default -> {
			// nothing to do
		}
		}
		r.drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
	}

	@Override
	protected void drawSceneInfo(GraphicsContext g) {
		Rendering2D.drawTileGrid(g, TILES_X, TILES_Y);
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery(GraphicsContext g) {
		int tx = ic.leftTileX;
		if (ic.titleVisible) {
			Rendering2D.drawText(g, "CHARACTER / NICKNAME", ArcadeTheme.PALE, Game2d.assets.arcadeFont, t(tx + 3), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!ic.ghostInfo[id].pictureVisible) {
				continue;
			}
			int ty = 7 + 3 * id;
			r.drawGhostFacingRight(g, id, t(tx) + 4, t(ty));
			if (ic.ghostInfo[id].characterVisible) {
				var text = "-" + ic.ghostInfo[id].character;
				var color = ArcadeTheme.GHOST_COLORS[id].dress();
				Rendering2D.drawText(g, text, color, Game2d.assets.arcadeFont, t(tx + 3), t(ty + 1));
			}
			if (ic.ghostInfo[id].nicknameVisible) {
				var text = QUOTE + ic.ghostInfo[id].ghost.name() + QUOTE;
				var color = ArcadeTheme.GHOST_COLORS[id].dress();
				Rendering2D.drawText(g, text, color, Game2d.assets.arcadeFont, t(tx + 14), t(ty + 1));
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if (Boolean.TRUE.equals(ic.blinking.frame())) {
			g.setFill(ArcadeTheme.PACMAN_MAZE_COLORS.foodColor());
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
		int tx = ic.leftTileX + 6;
		int ty = 25;
		g.setFill(ArcadeTheme.PACMAN_MAZE_COLORS.foodColor());
		g.fillRect(t(tx) + 4, t(ty - 1) + 4, 2, 2);
		if (Boolean.TRUE.equals(ic.blinking.frame())) {
			g.fillOval(t(tx), t(ty + 1), TS, TS);
		}
		g.setFill(ArcadeTheme.PALE);
		g.setFont(Game2d.assets.arcadeFont);
		g.fillText("10", t(tx + 2), t(ty));
		g.setFont(Game2d.assets.arcadeFont6); // TODO looks ugly
		g.fillText("PTS", t(tx + 5), t(ty));
		g.setFont(Game2d.assets.arcadeFont);
		g.fillText("50", t(tx + 2), t(ty + 2));
		g.setFont(Game2d.assets.arcadeFont6); // TODO still looks ugly
		g.fillText("PTS", t(tx + 5), t(ty + 2));
	}
}