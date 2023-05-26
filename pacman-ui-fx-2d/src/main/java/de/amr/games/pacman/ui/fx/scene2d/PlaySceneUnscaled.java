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
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 2D play scene that does not use scaled canvas.
 * 
 * @author Armin Reichert
 */
public class PlaySceneUnscaled extends GameScene2D {

	private GraphicsContext g;
	private Spritesheet ss;
	private Font f8;
	private Color tc;

	private double s(double value) {
		return value * fxSubScene.getHeight() / HEIGHT;
	}

	public PlaySceneUnscaled() {
		canvas.scaleXProperty().unbind();
		canvas.scaleYProperty().unbind();
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
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
		context.level().ifPresent(this::updateSound);
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

		g = canvas.getGraphicsContext2D();
		ss = r().spritesheet();
		f8 = r().theme().font("font.arcade", s(8));
		tc = ArcadeTheme.PALE;

		g.setFill(r().theme().color("wallpaper.color"));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 20, 20);

		if (context.isScoreVisible()) {
			drawScore(context.game().score(), "SCORE", t(1), t(1));
			drawScore(context.game().highScore(), "HIGH SCORE", t(16), t(1));
		}
		if (context.isCreditVisible()) {
			drawCredit(context.game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent();
		if (infoVisiblePy.get()) {
			drawSceneInfo();
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
				GameRenderer.drawText(g, "TEST    L%d".formatted(levelNumber), ArcadeTheme.YELLOW, f8, s(t(8.5)), s(t(21)));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				GameRenderer.drawText(g, "GAME  OVER", ArcadeTheme.RED, f8, s(t(9)), s(t(21)));
			} else if (context.state() == GameState.READY) {
				GameRenderer.drawText(g, "READY!", ArcadeTheme.YELLOW, f8, s(t(11)), s(t(21)));
			}
			level.bonusManagement().getBonus().ifPresent(this::drawBonus);
			drawPacSprite(level.pac());
			drawGhostSprite(level.ghost(GameModel.ORANGE_GHOST));
			drawGhostSprite(level.ghost(GameModel.CYAN_GHOST));
			drawGhostSprite(level.ghost(GameModel.PINK_GHOST));
			drawGhostSprite(level.ghost(GameModel.RED_GHOST));

			if (!context.isCreditVisible()) {
				// TODO get rid of this crap:
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				drawLivesCounter(lives);
			}
			drawLevelCounter(t(24), t(34), context.game().levelCounter());
		});
	}

	private void drawScore(Score score, String title, double x, double y) {
		GameRenderer.drawText(g, title, tc, f8, s(x), s(y));
		var pointsText = "%02d".formatted(score.points());
		GameRenderer.drawText(g, "%7s".formatted(pointsText), tc, f8, s(x), s((y + TS + 1)));
		if (score.points() != 0) {
			GameRenderer.drawText(g, "L%d".formatted(score.levelNumber()), tc, f8, s((x + TS * 8)), s((y + TS + 1)));
		}
	}

	private void drawCredit(int credit, double x, double y) {
		GameRenderer.drawText(g, "CREDIT %2d".formatted(credit), tc, f8, s(x), s(y));
	}

	private void drawPacManMaze(double x, double y, World world) {
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			var image = flashing ? r().theme().image("pacman.flashingMaze") : r().theme().image("pacman.emptyMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
		} else {
			var image = r().theme().image("pacman.fullMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
			world.tiles().filter(world::containsEatenFood).forEach(this::hideTileContent);
			var energizerBlinking = world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING);
			boolean energizerVisible = energizerBlinking.isPresent() && (boolean) energizerBlinking.get().frame();
			if (!energizerVisible) {
				world.energizerTiles().forEach(this::hideTileContent);
			}
		}
	}

	private void drawMsPacManMaze(double x, double y, int mazeNumber, World world) {
		var mpr = (MsPacManGameRenderer) r();
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			if (flashing) {
				var source = r().theme().image("mspacman.flashingMazes");
				var flashingMazeSprite = mpr.highlightedMaze(mazeNumber);
				drawSprite(source, flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(ss.source(), mpr.emptyMaze(mazeNumber), x, y);
			}
		} else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(mpr.filledMaze(mazeNumber), x, y);
			world.tiles().filter(world::containsEatenFood).forEach(this::hideTileContent);
			// energizer animation
			world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(blinking -> {
				if (Boolean.FALSE.equals(blinking.frame())) {
					world.energizerTiles().forEach(this::hideTileContent);
				}
			});
		}
	}

	private void hideTileContent(Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		// TODO check reason for blitzers and remove the workaround
		g.fillRect(s(TS * tile.x()) - 1, s(TS * tile.y()) - 1, s(TS) + 2, s(TS) + 2);
	}

	private void drawBonus(Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case Bonus.STATE_INACTIVE -> null;
		case Bonus.STATE_EDIBLE -> r().bonusSymbolSprite(bonus.symbol());
		case Bonus.STATE_EATEN -> r().bonusValueSprite(bonus.symbol());
		default -> throw new IllegalArgumentException();
		};
		if (sprite == null) {
			return;
		}
		var x = bonus.entity().position().x() + HTS - sprite.getWidth() / 2;
		var y = bonus.entity().position().y() + HTS - sprite.getHeight() / 2;
		if (bonus instanceof MovingBonus movingBonus) {
			g.save();
			g.translate(0, movingBonus.dy());
			drawSprite(sprite, x, y);
			g.restore();
		} else {
			drawSprite(sprite, x, y);
		}
	}

	private void drawPacSprite(Pac pac) {
		pac.animation().ifPresent(animation -> {
			if (pac.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = pac.position().x() + HTS - sprite.getWidth() / 2;
				var y = pac.position().y() + HTS - sprite.getHeight() / 2;
				// TODO check the blitzer cause and remove -1 workaround
				g.drawImage(ss.source(), sprite.getMinX(), sprite.getMinY(), sprite.getWidth() - 1, sprite.getHeight() - 1,
						s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
			}
		});
	}

	private void drawGhostSprite(Ghost ghost) {
		ghost.animation().ifPresent(animation -> {
			if (ghost.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = ghost.position().x() + HTS - sprite.getWidth() / 2;
				var y = ghost.position().y() + HTS - sprite.getHeight() / 2;
				drawSprite(sprite, x, y);
			}
		});
	}

	private void drawLivesCounter(int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		var x = TS * 2;
		var y = TS * (World.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			// TODO check reason for blitzers
			drawSprite(r().livesCounterSprite(), x + TS * (2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			GameRenderer.drawText(g, "+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, s(8)),
					s(x + TS * 10), s(y + TS));
		}
	}

	private void drawLevelCounter(double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(r().bonusSymbolSprite(symbol), x, yr);
			x -= TS * 2;
		}
	}

	private void drawSprite(Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, //
					sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), //
					s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
		}
	}

	private void drawSprite(Rectangle2D sprite, double x, double y) {
		drawSprite(ss.source(), sprite, x, y);
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(g, TILES_X, TILES_Y);
		context.level().ifPresent(level -> {
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