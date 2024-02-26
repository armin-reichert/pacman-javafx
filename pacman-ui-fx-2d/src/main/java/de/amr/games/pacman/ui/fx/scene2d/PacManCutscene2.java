/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGamePacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.t;

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
	public boolean isCreditVisible() {
		return !context.gameController().hasCredit();
	}

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;
		setScoreVisible(true);
		var ss = context.<PacManGameSpriteSheet>spriteSheet();
		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacManGamePacAnimations(pac, ss));
		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		var blinkyAnimations = new PacManGameGhostAnimations(blinky, ss);
		blinkyNormal = blinkyAnimations.animation(Ghost.ANIM_GHOST_NORMAL);
		blinkyStretching = blinkyAnimations.animation(Ghost.ANIM_BLINKY_STRETCHED);
		blinkyDamaged = blinkyAnimations.animation(Ghost.ANIM_BLINKY_DAMAGED);
		blinky.setAnimations(blinkyAnimations);
		blinky.setSpeed(0);
		blinky.hide();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				publishGameEvent(context.game(), GameEventType.INTERMISSION_STARTED);
			}
			return;
		}

		if (context.gameState().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
			case 1 -> blinkyStretching.setFrameIndex(0); // Show nail
			case 25 -> {
				pac.placeAtTile(28, 20, 0, 0);
				pac.setMoveDir(Direction.LEFT);
				pac.setSpeed(1.15f);
				pac.selectAnimation(Pac.ANIM_MUNCHING);
				pac.startAnimation();
				pac.show();
			}
			case 111 -> {
				blinky.placeAtTile(28, 20, -3, 0);
				blinky.setMoveAndWishDir(Direction.LEFT);
				blinky.setSpeed(1.25f);
				blinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
				blinky.startAnimation();
				blinky.show();
			}
			case 194 -> {
				blinky.setSpeed(0.09f);
				blinkyNormal.setFrameTicks(32);
			}
			case 198, 226, 248 -> blinkyStretching.nextFrame(); // Stretched S-M-L
			case 328 -> {
				blinky.setSpeed(0);
				blinkyStretching.nextFrame(); // Rapture
			}
			case 329 ->	blinky.selectAnimation(Ghost.ANIM_BLINKY_DAMAGED); // Eyes up
			case 389 ->	blinkyDamaged.nextFrame(); // Eyes right-down
			case 508 -> {
				blinky.setVisible(false);
				context.gameState().timer().expire();
			}
			default -> {}
		}

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
		drawTileGrid(ArcadeWorld.TILES_X, ArcadeWorld.TILES_Y);
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}