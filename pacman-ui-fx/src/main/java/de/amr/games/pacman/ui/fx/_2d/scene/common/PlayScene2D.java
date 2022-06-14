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
		guysInfo.init(game);
		creditVisible = !hasCredit();
		game.levelCounter.visible = hasCredit();
		game.setMazeFlashingAnimation(r2D.createMazeFlashingAnimation(r2D.mazeNumber(game.level.number)));
		game.pac.setAnimations(new PacAnimations(r2D));
		for (var ghost : game.ghosts) {
			ghost.setAnimations(new GhostAnimations(ghost.id, r2D));
		}
		game.bonus().setInactive();
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && game.credit() == 0) {
			gameController.state().addCredit(game);
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
		if (Env.$debugUI.get()) {
			guysInfo.update();
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawScore(g, game.scores.gameScore);
		r2D.drawScore(g, game.scores.highScore);
		if (game.mazeFlashingAnimation().isPresent() && game.mazeFlashingAnimation().get().isRunning()) {
			g.drawImage((Image) game.mazeFlashingAnimation().get().frame(), 0, t(3));
		} else {
			r2D.drawWorld(g, game.level.world, r2D.mazeNumber(game.level.number), !game.energizerPulse.frame());
		}
		if (Env.$tilesVisible.get()) {
			r2D.drawGrid(g);
		}
		r2D.drawGameStateMessage(g, hasCredit() ? gameController.state() : GameState.GAME_OVER);
		r2D.drawPac(g, game.pac);
		r2D.drawGhosts(g, game.ghosts);
		r2D.drawBonus(g, game.bonus().entity());
		if (creditVisible) {
			r2D.drawCredit(g, game.credit());
		} else {
			r2D.drawLivesCounter(g, game.playing ? game.lives - 1 : game.lives);
		}
		r2D.drawLevelCounter(g, game.levelCounter);
	}

	public void onSwitchFrom3DScene() {
		game.pac.animations().get().ensureRunning();
		for (var ghost : game.ghosts) {
			ghost.animations().get().ensureRunning();
		}
	}
}