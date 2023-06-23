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
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.app.Signature;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.GhostAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

	private MsPacManIntro intro;
	private final Signature signature = new Signature();

	public MsPacManIntroScene(PacManGames2dUI ui) {
		super(ui);
	}

	@Override
	public void init() {
		var ss = (SpritesheetMsPacManGame) ui().spritesheet();

		setCreditVisible(true);
		setScoreVisible(true);

		intro = new MsPacManIntro();
		var msPacMan = intro.context().msPacMan;
		msPacMan.setAnimations(new PacAnimationsMsPacManGame(msPacMan, ss));
		msPacMan.selectAnimation(PacAnimations.MUNCHING);
		intro.context().ghosts.forEach(ghost -> {
			ghost.setAnimations(new GhostAnimationsMsPacManGame(ghost, ss));
			ghost.selectAnimation(GhostAnimations.GHOST_NORMAL);
		});

		signature.setNameFont(ui().theme().font("font.handwriting", 9));
		signature.hide();

		intro.changeState(MsPacManIntro.State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		ui().soundHandler().stopVoice();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			ui().startGame();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			ui().switchGameVariant();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			ui().startCutscenesTest();
		}
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(28, 36);
	}

	@Override
	public void drawSceneContent() {
		var ic = intro.context();
		var tx = ic.titlePosition.x();
		var ty = ic.titlePosition.y();
		var y0 = ic.stopY;
		drawMarquee();
		drawText("\"MS PAC-MAN\"", ArcadeTheme.ORANGE, sceneFont(), tx, ty);
		if (intro.state() == State.GHOSTS) {
			var ghost = ic.ghosts.get(ic.ghostIndex);
			var color = ui().theme().color(String.format("ghost.%d.color.normal.dress", ghost.id()));
			if (ghost.id() == GameModel.RED_GHOST) {
				drawText("WITH", ArcadeTheme.PALE, sceneFont(), tx, y0 + t(3));
			}
			var text = ghost.name().toUpperCase();
			var dx = text.length() < 4 ? t(1) : 0;
			drawText(text, color, sceneFont(), tx + t(3) + dx, y0 + t(6));
		} else if (intro.state() == State.MSPACMAN || intro.state() == State.READY_TO_PLAY) {
			drawText("STARRING", ArcadeTheme.PALE, sceneFont(), tx, y0 + t(3));
			drawText("MS PAC-MAN", ArcadeTheme.YELLOW, sceneFont(), tx, y0 + t(6));
		}
		ic.ghosts.forEach(ghost -> drawGhostSprite(ghost));
		drawPacSprite(ic.msPacMan);
		drawMsPacManCopyright(t(6), t(28));
		drawLevelCounter(t(24), t(34), game().levelCounter());
	}

	private void drawMarquee() {
		var on = intro.context().marqueeState();
		for (int i = 0; i < intro.context().numBulbs; ++i) {
			g.setFill(on.get(i) ? ArcadeTheme.PALE : ArcadeTheme.RED);
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