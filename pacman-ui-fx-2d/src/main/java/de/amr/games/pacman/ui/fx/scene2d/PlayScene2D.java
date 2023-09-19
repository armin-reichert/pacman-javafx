/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import javafx.scene.paint.Color;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.ui.fx.app.PacManGames2dApp.*;

/**
 * 2D play scene.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	@Override
	public void init() {
		setCreditVisible(!GameController.it().hasCredit());
		setScoreVisible(true);
	}

	@Override
	public void update() {
		game().level().ifPresent(this::updateSound);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(KEY_ADD_CREDIT, KEY_ADD_CREDIT_NUMPAD)) {
			if (!GameController.it().hasCredit()) {
				context.actionHandler().addCredit();
			}
		} else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
			context.actionHandler().cheatEatAllPellets();
		} else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
			context.actionHandler().cheatAddLives();
		} else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
			context.actionHandler().cheatEnterNextLevel();
		} else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
			context.actionHandler().cheatKillAllEatableGhosts();
		}
	}

	@Override
	protected void drawSceneContent() {
		var theme = context.theme();
		game().level().ifPresent(level -> {
			if (game().variant() == GameVariant.MS_PACMAN) {
				int mazeNumber = game().mazeNumber(level.number());
				drawMsPacManMaze(level, mazeNumber);
			} else {
				drawPacManMaze(level);
			}
			if (state() == GameState.LEVEL_TEST) {
				drawText(String.format("TEST    L%d", level.number()),
						theme.color("palette.yellow"), sceneFont(8), t(8.5), t(21));
			} else if (state() == GameState.GAME_OVER || !GameController.it().hasCredit()) {
				drawText("GAME  OVER", theme.color("palette.red"), sceneFont(8), t(9), t(21));
			} else if (state() == GameState.READY) {
				drawText("READY!", theme.color("palette.yellow"), sceneFont(8), t(11), t(21));
			}
			level.bonus().ifPresent(this::drawBonus);
			drawPac(level.pac());
			Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
					.map(level::ghost).forEach(this::drawGhost);
			if (!isCreditVisible()) {
				boolean hideOne = level.pac().isVisible() || GameController.it().state() == GameState.GHOST_DYING;
				int lives = hideOne ? game().lives() - 1 : game().lives();
				drawLivesCounter(lives);
			}
			drawLevelCounter();
		});
	}

	// TODO put all images into a single sprite sheet
	private void drawPacManMaze(GameLevel level) {
		var theme = context.theme();
		var world = level.world();
		double x = 0, y = t(3);
		if (world.mazeFlashing().isRunning()) {
			var image = world.mazeFlashing().on()
					? theme.image("pacman.flashingMaze")
					: theme.image("pacman.emptyMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
		} else {
			var image = theme.image("pacman.fullMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
			world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
			if (world.energizerBlinking().off()) {
				world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
			}
		}
	}

	private void drawMsPacManMaze(GameLevel level, int mazeNumber) {
		var theme = context.theme();
		var world = level.world();
		double x = 0, y = t(3);
		var ss = (SpritesheetMsPacManGame) context.spritesheet();
		if (world.mazeFlashing().isRunning()) {
			if (world.mazeFlashing().on()) {
				var source = theme.image("mspacman.flashingMazes");
				var flashingMazeSprite = ss.highlightedMaze(mazeNumber);
				drawSprite(source, flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(ss.source(), ss.emptyMaze(mazeNumber), x, y);
			}
		} else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(ss.filledMaze(mazeNumber), x, y);
			world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
			// energizer animation
			if (world.energizerBlinking().off()) {
				world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
			}
		}
	}

	private void hideTileContent(World world, Vector2i tile) {
		g.setFill(Color.BLACK);
		double r = world.isEnergizerTile(tile) ? 4.5 : 2;
		double cx = t(tile.x()) + HTS;
		double cy = t(tile.y()) + HTS ;
		g.fillRect(s(cx-r), s(cy-r), s(2*r), s(2*r));
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(GameModel.TILES_X, GameModel.TILES_Y);
		game().level().ifPresent(level -> level.upwardsBlockedTiles().forEach(tile -> {
			// "No Trespassing" symbol
			g.setFill(Color.RED);
			g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
			g.setFill(Color.WHITE);
			g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
		}));
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		if (e.newState == GameState.GAME_OVER) {
			setCreditVisible(true);
		}
	}

	@Override
	public void onSceneVariantSwitch() {
		game().level().ifPresent(level -> {
			if (!level.isDemoLevel() && GameController.it().state() == GameState.HUNTING) {
				context.soundHandler().ensureSirenStarted(level.game().variant(), level.huntingPhase() / 2);
			}
		});
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			context.clip("audio.pacman_munch").stop();
		}
		if (!level.thisFrame().pacKilled && level.ghosts(RETURNING_TO_HOUSE, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
			context.soundHandler().ensureLoopEndless(context.clip("audio.ghost_returning"));
		} else {
			context.clip("audio.ghost_returning").stop();
		}
	}
}