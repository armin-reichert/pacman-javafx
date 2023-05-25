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
import static de.amr.games.pacman.ui.fx.rendering2d.GameRenderer.drawTileGrid;

import java.util.List;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.GestureHandler;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PacManPlayScene2D extends GameScene2D {

	public void addTouchSupport() {
		var touchPad = new Rectangle(WIDTH, HEIGHT);
		touchPad.setScaleX(0.9);
		touchPad.setScaleY(0.9);
		overlay.getChildren().add(touchPad);
		var gestureHandler = new GestureHandler(touchPad);
		gestureHandler.setOnDirectionRecognized(dir -> {
			context.game().level().ifPresent(level -> {
				level.pac().setWishDir(dir);
			});
		});
	}

	public PacManPlayScene2D() {
		canvas.scaleXProperty().unbind();
		canvas.scaleYProperty().unbind();
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
	public void init() {
		context.setCreditVisible(!context.hasCredit());
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> updateSound(level));
	}

	@Override
	public void end() {
		context.ui().stopAllSounds();
	}

	@Override
	public void render() {
		if (context == null) {
			return;
		}
		var g = canvas.getGraphicsContext2D();
		var r = context.renderer();
		var s = fxSubScene.getHeight() / HEIGHT;

		g.setFill(r.theme().color("wallpaper.color"));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 20, 20);

		if (context.isScoreVisible()) {
			drawScore(g, s, context.game().score(), "SCORE", t(1), t(1));
			drawScore(g, s, context.game().highScore(), "HIGH SCORE", t(16), t(1));
		}
		if (context.isCreditVisible()) {
			drawCredit(g, s, context.game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent(g);
		if (infoVisiblePy.get()) {
			drawSceneInfo(g);
		}
	}

	public void drawScore(GraphicsContext g, double s, Score score, String title, double x, double y) {
		var theme = context.ui().theme();
		var font = theme.font("font.arcade", 8 * s);
		GameRenderer.drawText(g, title, ArcadeTheme.PALE, font, s * x, s * y);
		var pointsText = "%02d".formatted(score.points());
		GameRenderer.drawText(g, "%7s".formatted(pointsText), ArcadeTheme.PALE, font, s * x, s * (y + TS + 1));
		if (score.points() != 0) {
			GameRenderer.drawText(g, "L%d".formatted(score.levelNumber()), ArcadeTheme.PALE, font, s * (x + TS * 8),
					s * (y + TS + 1));
		}
	}

	public void drawCredit(GraphicsContext g, double s, int credit, double x, double y) {
		var theme = context.ui().theme();
		var font = theme.font("font.arcade", 8 * s);
		GameRenderer.drawText(g, "CREDIT %2d".formatted(credit), ArcadeTheme.PALE, font, s * x, s * y);
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var s = fxSubScene.getHeight() / HEIGHT;
		canvas.setWidth(s * WIDTH);
		canvas.setHeight(s * HEIGHT);
		var r = context.renderer();
		context.level().ifPresent(level -> {
			int levelNumber = level.number();
			int mazeNumber = level.game().mazeNumber(levelNumber);
			drawMaze(g, s, 0, t(3), mazeNumber, level.world());
			var font8 = r.theme().font("font.arcade", 8 * s);
			if (context.state() == GameState.LEVEL_TEST) {
				GameRenderer.drawText(g, "TEST    L%d".formatted(levelNumber), ArcadeTheme.YELLOW, font8, s * t(8.5),
						s * t(21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				GameRenderer.drawText(g, "GAME  OVER", ArcadeTheme.RED, font8, s * t(9), s * t(21));
			} else if (context.state() == GameState.READY) {
				GameRenderer.drawText(g, "READY!", ArcadeTheme.YELLOW, font8, s * t(11), s * t(21));
			}
			level.bonusManagement().getBonus().ifPresent(bonus -> drawBonus(g, s, r.spritesheet(), bonus));

			drawPacSprite(level.pac(), g, r.spritesheet(), s);
			drawGhostSprite(level.ghost(GameModel.ORANGE_GHOST), g, r.spritesheet(), s);
			drawGhostSprite(level.ghost(GameModel.CYAN_GHOST), g, r.spritesheet(), s);
			drawGhostSprite(level.ghost(GameModel.PINK_GHOST), g, r.spritesheet(), s);
			drawGhostSprite(level.ghost(GameModel.RED_GHOST), g, r.spritesheet(), s);

			if (!context.isCreditVisible()) {
				// TODO get rid of this crap:
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				drawLivesCounter(g, s, r.spritesheet(), lives);
			}
			drawLevelCounter(g, s, t(24), t(34), context.game().levelCounter());
		});
	}

	private void drawMaze(GraphicsContext g, double s, double x, double y, int mazeNumber, World world) {
		var theme = context.ui().theme();
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			var image = flashing ? theme.image("pacman.flashingMaze") : theme.image("pacman.emptyMaze");
			g.drawImage(image, s * x, s * y, s * image.getWidth(), s * image.getHeight());
		} else {
			var image = theme.image("pacman.fullMaze");
			g.drawImage(image, 0, s * t(3), s * image.getWidth(), s * image.getHeight());
			world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, s, tile));
			var energizerBlinking = world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING);
			boolean energizerVisible = energizerBlinking.isPresent() && (boolean) energizerBlinking.get().frame();
			if (!energizerVisible) {
				world.energizerTiles().forEach(tile -> hideTileContent(g, s, tile));
			}
		}
	}

	private void hideTileContent(GraphicsContext g, double s, Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		// TODO check reason for blitzers and remove the workaround
		g.fillRect(s * TS * tile.x() - 1, s * TS * tile.y() - 1, s * TS + 2, s * TS + 2);
	}

	private void drawBonus(GraphicsContext g, double s, Spritesheet ss, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case Bonus.STATE_INACTIVE -> null;
		case Bonus.STATE_EDIBLE -> context.renderer().bonusSymbolSprite(bonus.symbol());
		case Bonus.STATE_EATEN -> context.renderer().bonusValueSprite(bonus.symbol());
		default -> throw new IllegalArgumentException();
		};
		if (sprite != null) {
			var x = bonus.entity().position().x() + HTS - sprite.getWidth() / 2;
			var y = bonus.entity().position().y() + HTS - sprite.getHeight() / 2;
			drawSprite(g, s, ss.source(), sprite, x, y);
		}
	}

	private void drawPacSprite(Pac pac, GraphicsContext g, Spritesheet ss, double s) {
		pac.animation().ifPresent(animation -> {
			if (pac.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = pac.position().x() + HTS - sprite.getWidth() / 2;
				var y = pac.position().y() + HTS - sprite.getHeight() / 2;
				// TODO check the blitzer cause and remove -1 workaround
				g.drawImage(ss.source(), sprite.getMinX(), sprite.getMinY(), sprite.getWidth() - 1, sprite.getHeight() - 1,
						s * x, s * y, s * sprite.getWidth(), s * sprite.getHeight());
			}
		});
	}

	private void drawGhostSprite(Ghost ghost, GraphicsContext g, Spritesheet ss, double s) {
		ghost.animation().ifPresent(animation -> {
			if (ghost.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = ghost.position().x() + HTS - sprite.getWidth() / 2;
				var y = ghost.position().y() + HTS - sprite.getHeight() / 2;
				drawSprite(g, s, ss.source(), sprite, x, y);
			}
		});
	}

	private void drawLivesCounter(GraphicsContext g, double s, Spritesheet ss, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		var x = TS * 2;
		var y = TS * (World.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			// TODO check reason for blitzers
			drawSprite(g, s, ss.source(), context.renderer().livesCounterSprite(), x + TS * (2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			GameRenderer.drawText(g, "+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, 8 * s),
					s * (x + TS * 10), s * (y + TS));
		}
	}

	private void drawLevelCounter(GraphicsContext g, double s, double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(g, s, context.renderer().spritesheet().source(), context.renderer().bonusSymbolSprite(symbol), x, yr);
			x -= TS * 2;
		}
	}

	private void drawSprite(GraphicsContext g, double s, Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), s * x, s * y,
					s * sprite.getWidth(), s * sprite.getHeight());
		}
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