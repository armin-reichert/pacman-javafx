/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.fx.app.ActionHandler;
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
				actionHandler().ifPresent(ActionHandler::addCredit);
			}
		} else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
			actionHandler().ifPresent(ActionHandler::cheatEatAllPellets);
		} else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
			actionHandler().ifPresent(ActionHandler::cheatAddLives);
		} else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
			actionHandler().ifPresent(ActionHandler::cheatEnterNextLevel);
		} else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
			actionHandler().ifPresent(ActionHandler::cheatKillAllEatableGhosts);
		}
	}

	@Override
	protected void drawSceneContent() {
		game().level().ifPresent(level -> {
			if (game().variant() == GameVariant.MS_PACMAN) {
				int mazeNumber = level.game().mazeNumber(level.number());
				drawMsPacManMaze(level, mazeNumber);
			} else {
				drawPacManMaze(level);
			}
			if (state() == GameState.LEVEL_TEST) {
				drawText(String.format("TEST    L%d", level.number()), theme.color("palette.yellow"), sceneFont()
						, t(8.5), t(21));
			} else if (state() == GameState.GAME_OVER || !GameController.it().hasCredit()) {
				drawText("GAME  OVER", theme.color("palette.red"), sceneFont(), t(9), t(21));
			} else if (state() == GameState.READY) {
				drawText("READY!", theme.color("palette.yellow"), sceneFont(), t(11), t(21));
			}
			level.getBonus().ifPresent(this::drawBonus);
			drawPac(level.pac());
			Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
					.map(level::ghost).forEach(this::drawGhost);
			if (!isCreditVisible()) {
				// TODO get rid of this crap:
				int lives = game().isOneLessLifeDisplayed() ? game().lives() - 1 : game().lives();
				drawLivesCounter(lives);
			}
			drawLevelCounter();
		});
	}

	// TODO put all images into a single spritesheet
	private void drawPacManMaze(GameLevel level) {
		double x = 0, y = t(3);
		if (level.world().mazeFlashing().isRunning()) {
			var image = level.world().mazeFlashing().on() ? theme.image("pacman.flashingMaze")
					: theme.image("pacman.emptyMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
		} else {
			var image = theme.image("pacman.fullMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
			level.world().tiles().filter(level.world()::hasEatenFoodAt).forEach(this::hideTileContent);
			if (level.world().energizerBlinking().off()) {
				level.world().energizerTiles().forEach(this::hideTileContent);
			}
		}
	}

	private void drawMsPacManMaze(GameLevel level, int mazeNumber) {
		double x = 0, y = t(3);
		var ss = (SpritesheetMsPacManGame) spritesheet;
		if (level.world().mazeFlashing().isRunning()) {
			if (level.world().mazeFlashing().on()) {
				var source = theme.image("mspacman.flashingMazes");
				var flashingMazeSprite = ss.highlightedMaze(mazeNumber);
				drawSprite(source, flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(ss.source(), ss.emptyMaze(mazeNumber), x, y);
			}
		} else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(ss.filledMaze(mazeNumber), x, y);
			level.world().tiles().filter(level.world()::hasEatenFoodAt).forEach(this::hideTileContent);
			// energizer animation
			if (level.world().energizerBlinking().off()) {
				level.world().energizerTiles().forEach(this::hideTileContent);
			}
		}
	}

	private void hideTileContent(Vector2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(s(TS * tile.x() - 1), s(TS * tile.y() - 1), s(TS + 2), s(TS + 2));
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(TILES_X, TILES_Y);
		game().level().ifPresent(level -> level.upwardsBlockedTiles().forEach(tile -> {
			// "No Trespassing" symbol
			g.setFill(Color.RED);
			g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
			g.setFill(Color.WHITE);
			g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
		}));
	}

	@Override
	public void onGameStateChange(GameEvent e) {
		if (e.newState == GameState.GAME_OVER) {
			setCreditVisible(true);
		}
	}

	@Override
	public void onSceneVariantSwitch() {
		game().level().ifPresent(level -> {
			if (!level.isDemoLevel() && GameController.it().state() == GameState.HUNTING) {
				soundHandler.ensureSirenStarted(level.game().variant(), level.huntingPhase() / 2);
			}
		});
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		var gameVariant = level.game().variant();
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			soundHandler.audioClip(gameVariant, "audio.pacman_munch").stop();
		}
		if (!level.isPacKilled() && level.ghosts(RETURNING_TO_HOUSE, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
			soundHandler.ensureLoopEndless(soundHandler.audioClip(gameVariant, "audio.ghost_returning"));
		} else {
			soundHandler.audioClip(gameVariant, "audio.ghost_returning").stop();
		}
	}
}