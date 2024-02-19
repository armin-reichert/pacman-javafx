/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.PacManIntro;
import de.amr.games.pacman.controller.PacManIntro.State;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGamePacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

	private static final char QUOTE = '\"';

	private PacManIntro intro;

	@Override
	public boolean isCreditVisible() {
		return true;
	}

	@Override
	public void init() {
		setScoreVisible(true);
		intro = new PacManIntro();
		var ss = context.<PacManGameSpriteSheet>spriteSheet();
		intro.pacMan.setAnimations(new PacManGamePacAnimations(intro.pacMan, ss));
		intro.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, ss)));
		intro.blinking.reset();
		intro.changeState(State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2dUI.KEYS_ADD_CREDIT)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.pressed(PacManGames2dUI.KEYS_START_GAME)) {
			context.actionHandler().startGame();
		} else if (Keyboard.pressed(PacManGames2dUI.KEY_SELECT_VARIANT)) {
			context.actionHandler().switchGameVariant();
		} else if (Keyboard.pressed(PacManGames2dUI.KEY_PLAY_CUTSCENES)) {
			context.actionHandler().startCutscenesTest();
		}
	}

	@Override
	public void drawSceneContent() {
		var timer = intro.state().timer();
		drawGallery();
		switch (intro.state()) {
			case SHOWING_POINTS -> drawPoints();
			case CHASING_PAC -> {
				drawPoints();
				drawBlinkingEnergizer();
				drawGuys(flutter(timer.tick()));
				drawMidwayCopyright(t(4), t(32));
			}
			case CHASING_GHOSTS, READY_TO_PLAY -> {
				drawPoints();
				drawGuys(0);
				drawMidwayCopyright(t(4), t(32));
			}
			default -> {}
		}
		drawLevelCounter();
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var ss = context.<PacManGameSpriteSheet>spriteSheet();
		var font = sceneFont(8);

		int tx = intro.leftTileX;
		if (intro.titleVisible) {
			drawText("CHARACTER / NICKNAME", context.theme().color("palette.pale"), font, t(tx + 3), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			var ghostInfo = intro.ghostInfo[id];
			if (!ghostInfo.pictureVisible) {
				continue;
			}
			int ty = 7 + 3 * id;
			drawSpriteCenteredOverBox(ss.ghostFacingRight(id), t(tx) + 4, t(ty));
			if (ghostInfo.characterVisible) {
				var text = "-" + ghostInfo.character;
				var color = context.theme().color("ghost." + id + ".color");
				drawText(text, color, font, t(tx + 3), t(ty + 1));
			}
			if (ghostInfo.nicknameVisible) {
				var text = QUOTE + ghostInfo.ghost.name() + QUOTE;
				var color = context.theme().color("ghost." + id + ".color");
				drawText(text, color, font, t(tx + 14), t(ty + 1));
			}
		}
	}

	private void drawBlinkingEnergizer() {
		if (intro.blinking.on()) {
			g.setFill(context.theme().color("pacman.maze.foodColor"));
			g.fillOval(s(t(intro.leftTileX)), s(t(20)), s(TS), s(TS));
		}
	}

	private void drawGuys(int shakingAmount) {
		if (shakingAmount == 0) {
			intro.ghosts().forEach(this::drawGhost);
		} else {
			drawGhost(intro.ghost(0));
			drawGhost(intro.ghost(3));
			// shaking ghosts effect, not quite as in original game
			g.save();
			g.translate(shakingAmount, 0);
			drawGhost(intro.ghost(1));
			drawGhost(intro.ghost(2));
			g.restore();
		}
		drawPac(intro.pacMan);
	}

	private void drawPoints() {
		var color = context.theme().color("palette.pale");
		var font8 = sceneFont(8);
		var font6 = sceneFont(6);
		int tx = intro.leftTileX + 6;
		int ty = 25;
		g.setFill(context.theme().color("pacman.maze.foodColor"));
		g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
		if (intro.blinking.on()) {
			g.fillOval(s(t(tx)), s(t(ty + 1)), s(TS), s(TS));
		}
		drawText("10",  color, font8, t(tx + 2), t(ty));
		drawText("PTS", color, font6, t(tx + 5), t(ty));
		drawText("50",  color, font8, t(tx + 2), t(ty + 2));
		drawText("PTS", color, font6, t(tx + 5), t(ty + 2));
	}
}