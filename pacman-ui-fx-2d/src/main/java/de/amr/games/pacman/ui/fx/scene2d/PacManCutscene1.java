/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene1 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	@Override
	public void init() {
		var ss = (SpritesheetPacManGame) spritesheet;

		setCreditVisible(true);
		setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacAnimationsPacManGame(pac, ss));
		pac.selectAnimation(PacAnimations.MUNCHING);
		pac.startAnimation();
		pac.placeAtTile(29, 20, 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setAnimations(new GhostAnimationsPacManGame(blinky, ss));
		blinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
		blinky.startAnimation();
		blinky.placeAtTile(32, 20, 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.3f);
		blinky.show();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameController.publishSoundEvent(SoundEvent.START_INTERMISSION_1);
			}
			return;
		}

		if (state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
		case 260: {
			blinky.placeAtTile(-2, 20, 4, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.setPixelSpeed(0.75f);
			blinky.selectAnimation(GhostAnimations.GHOST_FRIGHTENED);
			blinky.startAnimation();
			break;
		}

		case 400: {
			pac.placeAtTile(-3, 18, 0, 6.5f);
			pac.setMoveDir(Direction.RIGHT);
			pac.selectAnimation(PacAnimations.BIG_PACMAN);
			pac.startAnimation();
			break;
		}

		case 632: {
			state().timer().expire();
			break;
		}

		default: {
			pac.move();
			blinky.move();
			break;
		}

		}
	}

	@Override
	public void drawSceneContent() {
		drawPac(pac);
		drawGhost(blinky);
		drawLevelCounter(t(24), t(34), game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}