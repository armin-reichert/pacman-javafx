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

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private final ActorsInfo actorsInfo = new ActorsInfo(this);

	@Override
	public void init() {
		if (ctx.game().world() instanceof ArcadeWorld arcadeWorld) {
			arcadeWorld.setLevelCompleteAnimation(ctx.r2D().createMazeFlashingAnimation());
		}
		ctx.game().pac.setAnimationSet(ctx.r2D().createPacAnimationSet(ctx.game().pac));
		ctx.game().ghosts().forEach(ghost -> ghost.setAnimationSet(ctx.r2D().createGhostAnimationSet(ghost)));
		actorsInfo.enabledPy.bind(Env.showDebugInfoPy);
	}

	@Override
	public void update() {
		actorsInfo.update();
		setCreditVisible(!ctx.hasCredit());
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			if (!ctx.hasCredit()) { // credit can only be added in attract mode
				Actions.addCredit();
			}
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.L)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void draw(GraphicsContext g) {
		var mazePos = new V2i(0, 3 * TS);
		if (ctx.world() instanceof ArcadeWorld arcadeWorld) {
			var animation = arcadeWorld.levelCompleteAnimation();
			if (animation.isPresent() && animation.get().isRunning()) {
				boolean flash = (boolean) animation.get().frame();
				ctx.r2D().drawEmptyMaze(g, mazePos.x(), mazePos.y(), ctx.level().mazeNumber(), flash);
			} else {
				ctx.r2D().drawFilledMaze(g, mazePos.x(), mazePos.y(), ctx.level().mazeNumber(), ctx.world(),
						!ctx.game().energizerPulse.frame());
			}
		} else {
			ctx.r2D().drawFilledMaze(g, mazePos.x(), mazePos.y(), ctx.level().mazeNumber(), ctx.world(),
					!ctx.game().energizerPulse.frame());
		}
		ctx.r2D().drawGameStateMessage(g, ctx.hasCredit() ? ctx.state() : GameState.GAME_OVER);
		ctx.r2D().drawBonus(g, ctx.game().bonus());
		ctx.r2D().drawPac(g, ctx.game().pac);
		for (var ghost : ctx.game().theGhosts) {
			ctx.r2D().drawGhost(g, ghost);
		}
		if (!isCreditVisible()) {
			int lives = ctx.game().livesOneLessShown ? ctx.game().lives - 1 : ctx.game().lives;
			ctx.r2D().drawLivesCounter(g, lives);
		}
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter);
	}

	@Override
	public void onSwitchFrom3D() {
		ctx.game().pac.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		ctx.game().ghosts().map(Ghost::animationSet).forEach(anim -> anim.ifPresent(EntityAnimationSet::ensureRunning));
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		ctx.sounds().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}
}