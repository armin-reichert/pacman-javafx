/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.PacManIntro;
import de.amr.games.pacman.controller.PacManIntro.State;
import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;

import static de.amr.games.pacman.lib.Globals.TS;

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

	private PacManIntro.Context context() {
		return intro.context();
	}

	@Override
	public void init() {
		var ss = (SpritesheetPacManGame) spritesheet;

		setCreditVisible(true);
		setScoreVisible(true);

		intro = new PacManIntro();

		context().pacMan.setAnimations(new PacAnimationsPacManGame(context().pacMan, ss));
		context().ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsPacManGame(ghost, ss)));
		context().blinking.reset();

		intro.changeState(State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			actionHandler().ifPresent(ActionHandler::addCredit);
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			actionHandler().ifPresent(ActionHandler::startGame);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			actionHandler().ifPresent(ActionHandler::switchGameVariant);
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			actionHandler().ifPresent(ActionHandler::startCutscenesTest);
		}
	}

	@Override
	public void drawSceneContent() {
		var timer = intro.state().timer();
		drawGallery();
		switch (intro.state()) {
		case SHOWING_POINTS: {
			drawPoints();
			break;
		}
		case CHASING_PAC: {
			drawPoints();
			drawBlinkingEnergizer();
			drawGuys(flutter(timer.tick()));
			drawMidwayCopyright(t(4), t(32));
			break;
		}
		case CHASING_GHOSTS:
		case READY_TO_PLAY: {
			drawPoints();
			drawGuys(0);
			drawMidwayCopyright(t(4), t(32));
			break;
		}
		default:
			break;
		}
		drawLevelCounter(t(24), t(34), game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(TILES_X, TILES_Y);
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var ss = (SpritesheetPacManGame) spritesheet;
		var font = sceneFont();

		int tx = context().leftTileX;
		if (context().titleVisible) {
			drawText("CHARACTER / NICKNAME", ArcadeTheme.PALE, font, t(tx + 3), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			var ghostInfo = context().ghostInfo[id];
			if (!ghostInfo.pictureVisible) {
				continue;
			}
			int ty = 7 + 3 * id;
			drawSpriteOverBoundingBox(ss.ghostFacingRight(id), t(tx) + 4, t(ty));
			if (ghostInfo.characterVisible) {
				var text = "-" + ghostInfo.character;
				var color = theme.color("ghost." + id + ".color");
				drawText(text, color, font, t(tx + 3), t(ty + 1));
			}
			if (ghostInfo.nicknameVisible) {
				var text = QUOTE + ghostInfo.ghost.name() + QUOTE;
				var color = theme.color("ghost." + id + ".color");
				drawText(text, color, font, t(tx + 14), t(ty + 1));
			}
		}
	}

	private void drawBlinkingEnergizer() {
		if (context().blinking.on()) {
			g.setFill(theme.color("pacman.maze.foodColor"));
			g.fillOval(s(t(context().leftTileX)), s(t(20)), s(TS), s(TS));
		}
	}

	private void drawGuys(int shakingAmount) {
		if (shakingAmount == 0) {
			context().ghosts().forEach(ghost -> drawGhostSprite(ghost));
		} else {
			drawGhostSprite(context().ghost(0));
			drawGhostSprite(context().ghost(3));
			// shaking ghosts effect, not quite as in original game
			g.save();
			g.translate(shakingAmount, 0);
			drawGhostSprite(context().ghost(1));
			drawGhostSprite(context().ghost(2));
			g.restore();
		}
		drawPacSprite(context().pacMan);
	}

	private void drawPoints() {
		var font8 = sceneFont();
		var font6 = theme.font("font.arcade", s(6));
		int tx = context().leftTileX + 6;
		int ty = 25;
		g.setFill(theme.color("pacman.maze.foodColor"));
		g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
		if (context().blinking.on()) {
			g.fillOval(s(t(tx)), s(t(ty + 1)), s(TS), s(TS));
		}
		drawText("10",  ArcadeTheme.PALE, font8, t(tx + 2), t(ty));
		drawText("PTS", ArcadeTheme.PALE, font6, t(tx + 5), t(ty));
		drawText("50",  ArcadeTheme.PALE, font8, t(tx + 2), t(ty + 2));
		drawText("PTS", ArcadeTheme.PALE, font6, t(tx + 5), t(ty + 2));
	}
}