/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.rendering2d.GameRenderer.drawText;
import static de.amr.games.pacman.ui.fx.rendering2d.GameRenderer.drawTileGrid;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.GestureHandler;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private GestureHandler gestureHandler;
	private Rectangle touchPad = new Rectangle(80, 50);

	public PlayScene2D() {
		touchPad.setTranslateX(0.5 * (GameScene2D.WIDTH - touchPad.getWidth()));
		touchPad.setTranslateY(0.5 * (GameScene2D.HEIGHT - touchPad.getHeight()) - 4);
		touchPad.setFill(Color.gray(0.25, 0.25));
		overlay.getChildren().add(touchPad);
		gestureHandler = new GestureHandler(touchPad);
		gestureHandler.setOnDirectionRecognized(dir -> {
			context.game().level().ifPresent(level -> {
				level.pac().setWishDir(dir);
			});
		});
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_ADD_CREDIT) || Keyboard.pressed(PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			if (!context.hasCredit()) {
				PacManGames2d.app.addCredit();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_EAT_ALL)) {
			PacManGames2d.app.cheatEatAllPellets();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_ADD_LIVES)) {
			PacManGames2d.app.cheatAddLives();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_NEXT_LEVEL)) {
			PacManGames2d.app.cheatEnterNextLevel();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_KILL_GHOSTS)) {
			PacManGames2d.app.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void init() {
		context.setCreditVisible(!context.hasCredit());
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> updateSound(level, context.sounds()));
	}

	@Override
	public void end() {
		context.sounds().stopAll();
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		context.level().ifPresent(level -> {
			var r = context.renderer();
			int levelNumber = level.number();
			int mazeNumber = level.game().mazeNumber(levelNumber);
			r.drawMaze(g, 0, t(3), mazeNumber, level.world());
			if (context.state() == GameState.LEVEL_TEST) {
				drawText(g, "TEST    L%d".formatted(levelNumber), ArcadeTheme.YELLOW, PacManGames2d.assets.arcadeFont, t(8.5),
						t(21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				drawText(g, "GAME  OVER", ArcadeTheme.RED, PacManGames2d.assets.arcadeFont, t(9), t(21));
			} else if (context.state() == GameState.READY) {
				drawText(g, "READY!", ArcadeTheme.YELLOW, PacManGames2d.assets.arcadeFont, t(11), t(21));
			}
			level.bonusManagement().getBonus().ifPresent(bonus -> r.drawBonus(g, bonus));
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(GameModel.ORANGE_GHOST));
			r.drawGhost(g, level.ghost(GameModel.CYAN_GHOST));
			r.drawGhost(g, level.ghost(GameModel.PINK_GHOST));
			r.drawGhost(g, level.ghost(GameModel.RED_GHOST));
			if (!context.isCreditVisible()) {
				// TODO get rid of this crap:
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				r.drawLivesCounter(g, lives);
			}
			r.drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
		});
	}

	@Override
	protected void drawSceneInfo(GraphicsContext g) {
		drawTileGrid(g, TILES_X, TILES_Y);
		context.level().ifPresent(level -> {
			level.upwardsBlockedTiles().forEach(tile -> {
				// No trespassing symbol
				g.setFill(Color.RED);
				g.fillOval(t(tile.x()), t(tile.y() - 1), TS, TS);
				g.setFill(Color.WHITE);
				g.fillRect(t(tile.x()) + 1, t(tile.y()) - HTS - 1, TS - 2, 2);
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
			level.pac().animations().ifPresent(AnimationMap::ensureRunning);
			level.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(AnimationMap::ensureRunning));
			if (!level.isDemoLevel()) {
				context.sounds().ensureSirenStarted(level.huntingPhase() / 2);
			}
		});
	}

	private void updateSound(GameLevel level, GameSounds sounds) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			sounds.stop(AudioClipID.PACMAN_MUNCH);
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			sounds.ensureLoop(AudioClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			sounds.stop(AudioClipID.GHOST_RETURNING);
		}
	}
}