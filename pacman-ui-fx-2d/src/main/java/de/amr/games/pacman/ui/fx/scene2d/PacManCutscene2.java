/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GhostSpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimations;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private GhostSpriteAnimations blinkyAnimations;

	@Override
	protected PacManGameSpritesheet gss() {
		return (PacManGameSpritesheet) super.gss();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacSpriteAnimations(pac, gss()));
		pac.selectAnimation(PacAnimations.PAC_MUNCHING);
		pac.startAnimation();
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.15f);
		pac.show();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinkyAnimations = new GhostSpriteAnimations(blinky, gss());
		blinky.setAnimations(blinkyAnimations);
		blinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
		blinky.startAnimation();
		blinky.placeAtTile(v2i(28, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
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

		case 110: {
			blinky.setPixelSpeed(1.25f);
			blinky.show();
			break;
		}

		case 196: {
			blinky.setPixelSpeed(0.17f);
			blinkyAnimations.getStretchedAnimation().nextFrame();
			break;
		}

		case 226: {
			blinkyAnimations.getStretchedAnimation().nextFrame();
			break;
		}

		case 248: {
			blinky.setPixelSpeed(0);
			blinkyAnimations.getStretchedAnimation().nextFrame();
			break;
		}

		case 328: {
			blinkyAnimations.getStretchedAnimation().nextFrame();
			break;
		}

		case 329: {
			blinky.selectAnimation(GhostAnimations.BLINKY_DAMAGED);
			blinky.startAnimation();
			break;
		}

		case 389: {
			blinky.selectAnimation(GhostAnimations.BLINKY_STRETCHED);
			blinky.startAnimation();
			break;
		}

		case 508: {
//TODO			stretchedDressAnimation = null;
			break;
		}

		case 509: {
			context.state().timer().expire();
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
//		if (stretchedDressAnimation != null) {
//			drawSprite((Rectangle2D) stretchedDressAnimation.frame(), t(14), t(19) + 3.0);
//		}
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