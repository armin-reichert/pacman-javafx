/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
import static de.amr.games.pacman.ui.fx._2d.rendering.common.GameRenderer.drawTileStructure;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	@Override
	public void update() {
		creditVisible = !context.hasCredit() || context.state() == GameState.GAME_OVER;
		context.level().ifPresent(this::renderSound);
	}

	@Override
	public void end() {
		context.level().ifPresent(level -> {
			var sound = GameUI.sounds(level.game());
			sound.stopAll();
		});
	}

	@Override
	public void drawSceneContent() {
		var game = context.game();
		var r = context.r2D();
		game.level().ifPresent(level -> {
			drawMaze(r, level.world(), game.mazeNumber(level.number()), 0, t(3));
			r.drawBonus(g, level.bonus());
			drawGameState(r);
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(Ghost.ID_ORANGE_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_CYAN_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_PINK_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_RED_GHOST));
			if (!creditVisible) {
				int lives = game.isOneLessLifeDisplayed() ? game.lives() - 1 : game.lives();
				r.drawLivesCounter(g, lives);
			}
			r.drawLevelCounter(g, game.levelCounter());
		});
	}

	private void drawGameState(Rendering2D r) {
		boolean showGameOverText = context.state() == GameState.GAME_OVER || !context.hasCredit();
		if (showGameOverText) {
			r.drawGameOverMessage(g);
		} else if (context.state() == GameState.READY) {
			r.drawGameReadyMessage(g);
		}
	}

	private void drawMaze(Rendering2D r, World world, int mazeNumber, int x, int y) {
		var flashing = world.animation("flashing");
		if (flashing.isPresent() && flashing.get().isRunning()) {
			boolean flash = (boolean) flashing.get().frame();
			r.drawEmptyMaze(g, x, y, mazeNumber, flash);
		} else {
			var energizerPulse = world.animation("energizerPulse");
			if (energizerPulse.isPresent()) {
				boolean dark = !(boolean) energizerPulse.get().frame();
				r.drawMaze(g, x, y, mazeNumber, world, dark);
			} else {
				r.drawMaze(g, x, y, mazeNumber, world, false);
			}
		}
	}

	@Override
	protected void drawOverlayPaneContent() {
		drawTileStructure(g, size.x() / World.TS, size.y() / World.TS);
		context.level().ifPresent(level -> {
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				g.setFill(Color.RED);
				arcadeWorld.upwardBlockedTiles().forEach(tile -> g.fillRect(tile.x() * TS, tile.y() * TS, TS, 1));
			}
		});
	}

	@Override
	public void onKeyPressed() {
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
	public void onSwitchFrom3D() {
		context.level().ifPresent(level -> {
			level.pac().animations().ifPresent(EntityAnimationMap::ensureRunning);
			level.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(EntityAnimationMap::ensureRunning));
		});
	}

	private void renderSound(GameLevel level) {
		var sound = GameUI.sounds(level.game());
		if (level.huntingTimer().isRunning() && level.huntingTimer().tick() == 1) {
			sound.ensureSirenStarted(level.huntingPhase() / 2);
		}
		if (level.memo().pacPowerLost) {
			sound.stop(GameSound.PACMAN_POWER);
			sound.ensureSirenStarted(level.huntingPhase() / 2);
		}
		if (level.memo().foodFoundTile.isPresent()) {
			sound.ensureLoop(GameSound.PACMAN_MUNCH, 4);
		}
		if (level.ghosts(GhostState.RETURNING_TO_HOUSE).count() > 0) {
			if (!sound.isPlaying(GameSound.GHOST_RETURNING)) {
				sound.loop(GameSound.GHOST_RETURNING, AudioClip.INDEFINITE);
			}
		} else {
			sound.stop(GameSound.GHOST_RETURNING);
		}
	}
}