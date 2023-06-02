/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private GhostAnimationsPacManGame blinkyAnimations;

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacAnimationsPacManGame(pac, context.ui().spritesheetPacManGame()));

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinkyAnimations = new GhostAnimationsPacManGame(blinky, context.ui().spritesheetPacManGame());
		blinky.setAnimations(blinkyAnimations);
		blinky.setPixelSpeed(0);
		blinky.hide();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2, context.game());
			}
			return;
		}

		if (context.state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
		case 1: {
			blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).setFrameIndex(0); // Nail
			break;
		}

		case 25: {
			pac.placeAtTile(28, 20, 0, 0);
			pac.setMoveDir(Direction.LEFT);
			pac.setPixelSpeed(1.15f);
			pac.selectAnimation(PacAnimations.MUNCHING);
			pac.startAnimation();
			pac.show();
			break;
		}

		case 111: {
			blinky.placeAtTile(28, 20, -3, 0);
			blinky.setMoveAndWishDir(Direction.LEFT);
			blinky.setPixelSpeed(1.25f);
			blinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
			blinky.startAnimation();
			blinky.show();
			break;
		}

		case 194: {
			blinky.setPixelSpeed(0.1f);
//			blinkyAnimations.selectedAnimation().setFrameDuration(8);
			break;
		}

		case 198: {
			blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).nextFrame(); // Stretched S
			break;
		}

		case 226: {
			blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).nextFrame(); // Stretched M
			break;
		}

		case 248: {
			blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).nextFrame(); // Stretched L
			break;
		}

		case 328: {
			blinky.setPixelSpeed(0);
			blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).nextFrame(); // Rapture
			break;
		}

		case 329: {
			blinky.selectAnimation(GhostAnimations.BLINKY_DAMAGED); // Eyes up
			break;
		}

		case 389: {
			blinkyAnimations.byName(GhostAnimations.BLINKY_DAMAGED).nextFrame(); // Eyes right-down
			break;
		}

		case 508: {
			blinky.setVisible(false);
			context.state().timer().expire();
			break;
		}

		default: {
			break;
		}

		} // switch

		blinky.move();
		pac.move();
	}

	@Override
	public void drawSceneContent() {
		drawSprite(blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED).currentSprite(), s(t(14)), s(t(19) + 3));
		drawGhostSprite(blinky);
		drawPacSprite(pac);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, ArcadeTheme.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}