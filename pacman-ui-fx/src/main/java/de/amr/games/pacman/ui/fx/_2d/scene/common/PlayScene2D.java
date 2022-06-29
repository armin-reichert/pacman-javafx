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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private final GuysInfo guysInfo = new GuysInfo(this);

	@Override
	public void init() {
		guysInfo.init(ctx.game);
		creditVisible = !hasCredit();
		ctx.game.levelCounter.visible = hasCredit();
		var world = (ArcadeWorld) ctx.game.world();
		world.setFlashingAnimation(ctx.r2D.createMazeFlashingAnimation(ctx.game.level.mazeNumber));
		ctx.game.pac.setAnimations(new PacAnimations(ctx.r2D));
		for (var ghost : ctx.game.theGhosts) {
			ghost.setAnimations(new GhostAnimations(ghost.id, ctx.r2D));
		}
		ctx.game.bonus().setInactive();
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && ctx.game.credit == 0) {
			ctx.gameState().addCredit(ctx.game);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.L)) {
			Actions.addLives(3);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	protected void doUpdate() {
		if (Env.debugUI.get()) {
			guysInfo.update();
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		ctx.r2D.drawScore(g, ctx.game.scores.gameScore);
		ctx.r2D.drawScore(g, ctx.game.scores.highScore);
		var world = (ArcadeWorld) ctx.game.world();
		var flashingAnimation = world.flashingAnimation();
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			g.drawImage((Image) flashingAnimation.get().frame(), t(0), t(3));
		} else {
			ctx.r2D.drawMaze(g, t(0), t(3), ctx.game.level.world, ctx.game.level.mazeNumber,
					!ctx.game.energizerPulse.frame());
		}
		ctx.r2D.drawGameStateMessage(g, hasCredit() ? ctx.gameState() : GameState.GAME_OVER);
		ctx.r2D.drawBonus(g, ctx.game.bonus());
		ctx.r2D.drawPac(g, ctx.game.pac);
		ctx.r2D.drawGhosts(g, ctx.game.theGhosts);
		if (creditVisible) {
			ctx.r2D.drawCredit(g, ctx.game.credit);
		} else {
			ctx.r2D.drawLivesCounter(g, ctx.game.playing ? ctx.game.lives - 1 : ctx.game.lives);
		}
		ctx.r2D.drawLevelCounter(g, ctx.game.levelCounter);
	}

	public void onSwitchFrom3D() {
		ctx.game.pac.animations().ifPresent(SpriteAnimations::ensureRunning);
		ctx.game.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(SpriteAnimations::ensureRunning));
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		ctx.gameController.sounds().ifPresent(snd -> snd.play(GameSound.BONUS_EATEN));
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.gameController.sounds().ifPresent(snd -> snd.play(GameSound.EXTRA_LIFE));
	}
}