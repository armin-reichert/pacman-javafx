/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * 2D play scene. Key <code>F12</code> toggles between scaled canvas and unscaled canvas.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	@Override
	public void init() {
		context.setCreditVisible(!context.hasCredit());
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> {
			updateSound(level);
		});
	}

	@Override
	public void end() {
		context.ui().stopAllSounds();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			if (!context.hasCredit()) {
				context.ui().addCredit();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_EAT_ALL)) {
			context.ui().cheatEatAllPellets();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_ADD_LIVES)) {
			context.ui().cheatAddLives();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_NEXT_LEVEL)) {
			context.ui().cheatEnterNextLevel();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_KILL_GHOSTS)) {
			context.ui().cheatKillAllEatableGhosts();
		}
	}

	@Override
	protected void drawSceneContent() {
		context.level().ifPresent(level -> {
			int levelNumber = level.number();
			if (context.gameVariant() == GameVariant.MS_PACMAN) {
				int mazeNumber = level.game().mazeNumber(levelNumber);
				drawMsPacManMaze(0, t(3), mazeNumber, level.world());
			} else {
				drawPacManMaze(0, t(3), level.world());
			}
			if (context.state() == GameState.LEVEL_TEST) {
				drawText(String.format("TEST    L%d", levelNumber), ArcadeTheme.YELLOW, sceneFont(), t(8.5), t(21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				drawText("GAME  OVER", ArcadeTheme.RED, sceneFont(), t(9), t(21));
			} else if (context.state() == GameState.READY) {
				drawText("READY!", ArcadeTheme.YELLOW, sceneFont(), t(11), t(21));
			}
			level.bonusManagement().getBonus().ifPresent(this::drawBonus);
			drawPacSprite(level.pac());
			Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
					.map(level::ghost).forEach(this::drawGhostSprite);
			if (!context.isCreditVisible()) {
				// TODO get rid of this crap:
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				drawLivesCounter(lives);
			}
			drawLevelCounter(t(24), t(34), context.game().levelCounter());
		});

	}

	private void drawPacManMaze(double x, double y, World world) {
		if (world.getMazeFlashing().isRunning()) {
			var image = world.getMazeFlashing().frame() ? context.ui().theme().image("pacman.flashingMaze")
					: context.ui().theme().image("pacman.emptyMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
		} else {
			var image = context.ui().theme().image("pacman.fullMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
			world.tiles().filter(world::containsEatenFood).forEach(this::hideTileContent);
			if (!world.getEnergizerBlinking().frame()) {
				world.energizerTiles().forEach(this::hideTileContent);
			}
		}
	}

	private void drawMsPacManMaze(double x, double y, int mazeNumber, World world) {
		var mpr = (MsPacManGameSpritesheet) gss();
		if (world.getMazeFlashing().isRunning()) {
			if (world.getMazeFlashing().frame()) {
				var source = context.ui().theme().image("mspacman.flashingMazes");
				var flashingMazeSprite = mpr.highlightedMaze(mazeNumber);
				drawSprite(source, flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(gss().sheet().source(), mpr.emptyMaze(mazeNumber), x, y);
			}
		} else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(mpr.filledMaze(mazeNumber), x, y);
			world.tiles().filter(world::containsEatenFood).forEach(this::hideTileContent);
			// energizer animation
			if (!world.getEnergizerBlinking().frame()) {
				world.energizerTiles().forEach(this::hideTileContent);
			}
		}
	}

	private void hideTileContent(Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		// TODO check reason for blitzers and remove the workaround
		g.fillRect(s(TS * tile.x()) - 1, s(TS * tile.y()) - 1, s(TS) + 2, s(TS) + 2);
	}

	private void drawBonus(Bonus bonus) {
		Rectangle2D sprite = null;
		switch (bonus.state()) {
		case Bonus.STATE_INACTIVE:
			break;
		case Bonus.STATE_EDIBLE:
			sprite = gss().bonusSymbolSprite(bonus.symbol());
			break;
		case Bonus.STATE_EATEN:
			sprite = gss().bonusValueSprite(bonus.symbol());
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (sprite == null) {
			return;
		}
		var x = bonus.entity().position().x() + HTS - sprite.getWidth() / 2;
		var y = bonus.entity().position().y() + HTS - sprite.getHeight() / 2;
		if (bonus instanceof MovingBonus) {
			var movingBonus = (MovingBonus) bonus;
			g.save();
			g.translate(0, movingBonus.dy());
			drawSprite(sprite, x, y);
			g.restore();
		} else {
			drawSprite(sprite, x, y);
		}
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(TILES_X, TILES_Y);
		context.level().ifPresent(level -> {
			drawWishDirIndicator(level.pac());
			level.upwardsBlockedTiles().forEach(tile -> {
				// "No Trespassing" symbol
				g.setFill(Color.RED);
				g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
				g.setFill(Color.WHITE);
				g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
			});
		});
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		if (e.newGameState == GameState.GAME_OVER) {
			context.setCreditVisible(true);
		}
	}

	@Override
	public void onSceneVariantSwitch() {
		context.level().ifPresent(level -> {
//TODO			level.pac().animations().ifPresent(AnimationMap::ensureRunning);
//TODO			level.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(AnimationMap::ensureRunning));
			if (!level.isDemoLevel()) {
				context.ui().ensureSirenStarted(level.huntingPhase() / 2);
			}
		});
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			context.ui().stopMunchingSound();
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			context.ui().loopGhostReturningSound();
		} else {
			context.ui().stopGhostReturningSound();
		}
	}
}