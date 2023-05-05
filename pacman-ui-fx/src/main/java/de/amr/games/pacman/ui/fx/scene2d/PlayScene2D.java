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
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawText;
import static de.amr.games.pacman.ui.fx.rendering2d.Rendering2D.drawTileStructure;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Game2d.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	public PlayScene2D(GameController gameController) {
		super(gameController);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.ADD_CREDIT) && !context.hasCredit()) {
			Game2d.Actions.addCredit();
		} else if (Keyboard.pressed(Keys.CHEAT_EAT_ALL)) {
			Game2d.Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keys.CHEAT_ADD_LIVES)) {
			Game2d.Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Keys.CHEAT_NEXT_LEVEL)) {
			Game2d.Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keys.CHEAT_KILL_GHOSTS)) {
			Game2d.Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void init() {
		context.setCreditVisible(!context.hasCredit());
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		if (context.state() == GameState.GAME_OVER) {
			context.setCreditVisible(true);
		}
		context.level().ifPresent(this::updateSound);
	}

	@Override
	public void end() {
		// TODO check if this is needed
		context.sounds().stopAll();
	}

	@Override
	public void drawScene(GraphicsContext g) {
		context.level().ifPresent(level -> {
			var r = context.rendering2D();
			var mazeNumber = level.game().mazeNumber(level.number());
			r.drawMaze(g, 0, TS * (3), mazeNumber, level.world());
			if (context.state() == GameState.LEVEL_TEST) {
				drawText(g, "TEST    L%d".formatted(level.number()), ArcadeTheme.YELLOW, r.screenFont(TS), TS * (8) + 4,
						TS * (21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				drawText(g, "GAME  OVER", ArcadeTheme.RED, r.screenFont(TS), TS * (9), TS * (21));
			} else if (context.state() == GameState.READY) {
				drawText(g, "READY!", ArcadeTheme.YELLOW, r.screenFont(TS), TS * (11), TS * (21));
			}
			level.bonusManagement().getBonus().ifPresent(bonus -> r.drawBonus(g, bonus));
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(GameModel.ORANGE_GHOST));
			r.drawGhost(g, level.ghost(GameModel.CYAN_GHOST));
			r.drawGhost(g, level.ghost(GameModel.PINK_GHOST));
			r.drawGhost(g, level.ghost(GameModel.RED_GHOST));
			if (!context.isCreditVisible()) {
				// TODO get rid of this crap
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				r.drawLivesCounter(g, lives);
			}
			drawLevelCounter(g);
		});
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		drawTileStructure(g, World.TILES_X, World.TILES_Y);
		context.level().ifPresent(level -> {
			level.upwardsBlockedTiles().forEach(tile -> {
				g.setFill(Color.RED);
				g.fillOval(tile.x() * TS, (tile.y() - 1) * TS, TS, TS);
				g.setFill(Color.WHITE);
				g.fillRect(tile.x() * TS + 1, tile.y() * TS - HTS - 1, TS - 2, 2);
			});
		});
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

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 10) {
			context.sounds().stop(AudioClipID.PACMAN_MUNCH);
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			context.sounds().ensureLoop(AudioClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			context.sounds().stop(AudioClipID.GHOST_RETURNING);
		}
	}
}