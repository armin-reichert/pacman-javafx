/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private SpriteAnimation blinkyNormal;
	private SpriteAnimation blinkyStretching;
	private SpriteAnimation blinkyDamaged;

	@Override
	public void init() {
		var ss = (SpritesheetPacManGame) spritesheet;

		setCreditVisible(!GameController.it().hasCredit());
		setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacAnimationsPacManGame(pac, ss));

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		var blinkyAnimations = new GhostAnimationsPacManGame(blinky, ss);
		blinkyNormal = blinkyAnimations.byName(GhostAnimations.GHOST_NORMAL);
		blinkyStretching = blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED);
		blinkyDamaged = blinkyAnimations.byName(GhostAnimations.BLINKY_DAMAGED);
		blinky.setAnimations(blinkyAnimations);
		blinky.setPixelSpeed(0);
		blinky.hide();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameController.it().publishGameEvent(GameEventType.INTERMISSION_STARTED);
			}
			return;
		}

		if (state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
		case 1: {
			blinkyStretching.setFrameIndex(0); // Show nail
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
			blinky.setPixelSpeed(0.09f);
			blinkyNormal.setFrameTicks(32);
			break;
		}

		case 198: {
			blinkyStretching.nextFrame(); // Stretched S
			break;
		}

		case 226: {
			blinkyStretching.nextFrame(); // Stretched M
			break;
		}

		case 248: {
			blinkyStretching.nextFrame(); // Stretched L
			break;
		}

		case 328: {
			blinky.setPixelSpeed(0);
			blinkyStretching.nextFrame(); // Rapture
			break;
		}

		case 329: {
			blinky.selectAnimation(GhostAnimations.BLINKY_DAMAGED); // Eyes up
			break;
		}

		case 389: {
			blinkyDamaged.nextFrame(); // Eyes right-down
			break;
		}

		case 508: {
			blinky.setVisible(false);
			state().timer().expire();
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
		drawSprite(blinkyStretching.currentSprite(), t(14), t(19) + 3);
		drawGhost(blinky);
		drawPac(pac);
		drawLevelCounter();
	}

	@Override
	protected void drawSceneInfo() {
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}