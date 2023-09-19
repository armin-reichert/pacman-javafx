/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.GhostAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

import static de.amr.games.pacman.ui.fx.app.PacManGames2dApp.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private MsPacManIntro intro;

	@Override
	public void init() {
		var ss = (SpritesheetMsPacManGame) context.spritesheet();

		setCreditVisible(true);
		setScoreVisible(true);

		intro = new MsPacManIntro();
		var msPacMan = intro.context().msPacMan;
		msPacMan.setAnimations(new PacAnimationsMsPacManGame(msPacMan, ss));
		msPacMan.selectAnimation(PacAnimations.MUNCHING);
		for (var ghost : intro.context().ghosts) {
			ghost.setAnimations(new GhostAnimationsMsPacManGame(ghost, ss));
			ghost.selectAnimation(GhostAnimations.GHOST_NORMAL);
		};

		intro.changeState(MsPacManIntro.State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(KEY_ADD_CREDIT, KEY_ADD_CREDIT_NUMPAD)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.anyPressed(KEY_START_GAME, KEY_START_GAME_NUMPAD)) {
			context.actionHandler().startGame();
		} else if (Keyboard.pressed(KEY_SELECT_VARIANT)) {
			context.actionHandler().switchGameVariant();
		} else if (Keyboard.pressed(KEY_PLAY_CUTSCENES)) {
			context.actionHandler().startCutscenesTest();
		}
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(28, 36);
	}

	@Override
	public void drawSceneContent() {
		var theme = context.theme();
		var font8 = sceneFont(8);
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.stopY;
		drawMarquee();
		drawText("\"MS PAC-MAN\"", theme.color("palette.orange"), font8, tx, ty);
		if (intro.state() == State.GHOSTS) {
			var ghost = ic.ghosts[ic.ghostIndex];
			var color = theme.color(String.format("ghost.%d.color", ghost.id()));
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText("WITH", theme.color("palette.pale"), font8, tx, y0 + t(3));
			}
			var text = ghost.name().toUpperCase();
			var dx = text.length() < 4 ? t(1) : 0;
			drawText(text, color, font8, tx + t(3) + dx, y0 + t(6));
		} else if (intro.state() == State.MSPACMAN || intro.state() == State.READY_TO_PLAY) {
			drawText("STARRING", theme.color("palette.pale"), font8, tx, y0 + t(3));
			drawText("MS PAC-MAN", theme.color("palette.yellow"), font8, tx, y0 + t(6));
		}
		for (var ghost : ic.ghosts) {
			drawGhost(ghost);
		}
		drawPac(ic.msPacMan);
		drawMsPacManCopyright(t(6), t(28));
		drawLevelCounter();
	}

	private void drawMarquee() {
		var theme = context.theme();
		var on = intro.context().marqueeState();
		for (int i = 0; i < intro.context().numBulbs; ++i) {
			g.setFill(on.get(i) ? theme.color("palette.pale") : theme.color("palette.red"));
			if (i <= 33) {
				g.fillRect(s(60 + 4 * i), s(148), s(2), s(2));
			} else if (i <= 48) {
				g.fillRect(s(192), s(280 - 4 * i), s(2), s(2));
			} else if (i <= 81) {
				g.fillRect(s(384 - 4 * i), s(88), s(2), s(2));
			} else {
				g.fillRect(s(60), s(4 * i - 236), s(2), s(2));
			}
		}
	}
}