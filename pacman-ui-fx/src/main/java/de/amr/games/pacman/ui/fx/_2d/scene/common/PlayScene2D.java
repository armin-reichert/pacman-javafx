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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D.drawText;
import static de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D.drawTileStructure;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MsPacManGameDemoLevel;
import de.amr.games.pacman.model.pacman.PacManGameDemoLevel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.sound.SoundClipID;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
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
			Actions.addCredit();
		} else if (Keyboard.pressed(Keys.CHEAT_EAT_ALL)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keys.CHEAT_ADD_LIVES)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Keys.CHEAT_NEXT_LEVEL)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keys.CHEAT_KILL_GHOSTS)) {
			Actions.cheatKillAllEatableGhosts();
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
		SoundHandler.sounds(context.game()).stopAll();
	}

	@Override
	public void drawScene(GraphicsContext g) {
		context.level().ifPresent(level -> {
			var r = context.rendering2D();
			var mazeNumber = level.game().mazeNumber(level.number());
			r.drawMaze(g, 0, t(3), mazeNumber, level.world());
			if (context.state() == GameState.LEVEL_TEST) {
				drawText(g, "TEST    L%d".formatted(level.number()), ArcadeTheme.YELLOW, r.screenFont(TS), t(8) + 4, t(21));
			} else if (context.state() == GameState.GAME_OVER || !context.hasCredit()) {
				drawText(g, "GAME  OVER", ArcadeTheme.RED, r.screenFont(TS), t(9), t(21));
			} else if (context.state() == GameState.READY) {
				drawText(g, "READY!", ArcadeTheme.YELLOW, r.screenFont(TS), t(11), t(21));
			}
			r.drawBonus(g, level.bonus());
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(Ghost.ID_ORANGE_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_CYAN_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_PINK_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_RED_GHOST));
			if (!context.isCreditVisible()) {
				// TODO get rid of this crap
				int lives = context.game().isOneLessLifeDisplayed() ? context.game().lives() - 1 : context.game().lives();
				r.drawLivesCounter(g, lives);
			}
			r.drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
		});
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		drawTileStructure(g, ArcadeWorld.SIZE_TILES.x(), ArcadeWorld.SIZE_TILES.y());
		context.level().ifPresent(level -> {
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				g.setFill(Color.RED);
				arcadeWorld.upwardBlockedTiles().forEach(tile -> g.fillRect(tile.x() * TS, tile.y() * TS, TS, 1));
			}
		});
	}

	@Override
	public void onSceneVariantSwitch() {
		context.level().ifPresent(level -> {
			level.pac().animations().ifPresent(AnimationMap::ensureRunning);
			level.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(AnimationMap::ensureRunning));
		});
	}

	private void updateSound(GameLevel level) {
		// TODO don't check for specific level, maybe mark level as silent
		if (level instanceof PacManGameDemoLevel || level instanceof MsPacManGameDemoLevel) {
			return;
		}
		var sounds = SoundHandler.sounds(level.game());
		if (level.pac().starvingTicks() > 10) {
			sounds.stop(SoundClipID.PACMAN_MUNCH);
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			sounds.ensureLoop(SoundClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			sounds.stop(SoundClipID.GHOST_RETURNING);
		}
	}
}