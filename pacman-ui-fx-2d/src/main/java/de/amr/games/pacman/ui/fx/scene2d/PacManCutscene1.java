/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManPacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutscene1 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	@Override
	public boolean isCreditVisible() {
		return !context.gameController().hasCredit();
	}

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;
		setScoreVisible(true);

		var ss = context.<PacManSpriteSheet>spriteSheet();

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacManPacAnimations(pac, ss));
		pac.selectAnimation(PacAnimations.MUNCHING);
		pac.startAnimation();
		pac.placeAtTile(29, 20, 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setAnimations(new PacManGhostAnimations(blinky, ss));
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
				context.gameController().publishGameEvent(GameEventType.INTERMISSION_STARTED);
			}
			return;
		}

		if (context.gameState().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
			case 260 -> {
				blinky.placeAtTile(-2, 20, 4, 0);
				blinky.setMoveAndWishDir(Direction.RIGHT);
				blinky.setPixelSpeed(0.75f);
				blinky.selectAnimation(GhostAnimations.GHOST_FRIGHTENED);
				blinky.startAnimation();
			}
			case 400 -> {
				pac.placeAtTile(-3, 18, 0, 6.5f);
				pac.setMoveDir(Direction.RIGHT);
				pac.selectAnimation(PacAnimations.BIG_PACMAN);
				pac.startAnimation();
			}
			case 632 -> context.gameState().timer().expire();
			default -> {}
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void drawSceneContent() {
		drawPac(pac);
		drawGhost(blinky);
		drawLevelCounter();
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(GameModel.TILES_X, GameModel.TILES_Y);
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}