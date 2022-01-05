/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	public Maze2D maze2D;
	public GameScore2D score2D;
	public GameScore2D hiscore2D;
	public LivesCounter2D livesCounter2D;
	public Player2D player2D;
	public List<Ghost2D> ghosts2D;
	public Bonus2D bonus2D;

	private final PlayScene2DWithAnimations animationController;

	public PlayScene2D(Rendering2D rendering, SoundManager sounds) {
		super(rendering, sounds, 28, 36);
		animationController = new PlayScene2DWithAnimations(this, sounds);
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		super.setGameController(gameController);
		animationController.setGameController(gameController);
	}

	@Override
	public void init() {
		maze2D = new Maze2D(game(), new V2i(0, 3), rendering);

		livesCounter2D = new LivesCounter2D(rendering);
		livesCounter2D.x = t(2);
		livesCounter2D.y = t(34);

		score2D = new GameScore2D(rendering);
		score2D.title = "SCORE";
		score2D.x = t(1);
		score2D.y = t(1);
		score2D.levelSupplier = () -> game().levelNumber;
		score2D.pointsSupplier = () -> game().score;

		hiscore2D = new GameScore2D(rendering);
		hiscore2D.title = "HIGH SCORE";
		hiscore2D.x = t(16);
		hiscore2D.y = t(1);
		hiscore2D.pointsSupplier = () -> game().hiscorePoints;
		hiscore2D.levelSupplier = () -> game().hiscoreLevel;

		player2D = new Player2D(game().player, rendering);
		player2D.dyingAnimation.delay(120).onStart(() -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		ghosts2D = game().ghosts().map(ghost -> new Ghost2D(ghost, rendering)).collect(Collectors.toList());

		bonus2D = new Bonus2D(game().bonus, rendering);

		game().player.powerTimer.addEventListener(animationController::handleGhostsFlashing);
		animationController.init();
	}

	@Override
	public void end() {
		game().player.powerTimer.removeEventListener(animationController::handleGhostsFlashing);
	}

	@Override
	public void doUpdate() {
		livesCounter2D.lives = game().player.lives;
		animationController.update();
	}

	private void onGameStateChange(PacManGameStateChangeEvent event) {
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			maze2D.onGameChanged(event.game);
			// wait 1 second
			gameController.stateTimer().reset(60);
			gameController.stateTimer().start();
			animationController.init();
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameEvent instanceof PacManGameStateChangeEvent) {
			onGameStateChange((PacManGameStateChangeEvent) gameEvent);
		}
		animationController.onGameEvent(gameEvent);
	}

	@Override
	public void doRender() {
		if (!gameController.isAttractMode()) {
			score2D.showPoints = true;
			livesCounter2D.render(gc);
			renderLevelCounter(new V2i(25, 34));
		} else {
			score2D.showPoints = false;
		}
		renderGameState();
		game().ghosts(GhostState.LOCKED)
				.forEach(ghost -> ghosts2D.get(ghost.id).setLooksFrightened(game().player.powerTimer.isRunning()));
		Stream.concat(Stream.of(score2D, hiscore2D, maze2D, bonus2D, player2D), ghosts2D.stream())
				.forEach(r -> r.render(gc));
	}

	private void renderGameState() {
		PacManGameState state = gameController.isAttractMode() ? PacManGameState.GAME_OVER
				: gameController.currentStateID;
		if (state == PacManGameState.GAME_OVER) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.RED);
			gc.fillText("GAME", t(9), t(21));
			gc.fillText("OVER", t(15), t(21));
		} else if (state == PacManGameState.READY) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.YELLOW);
			gc.fillText("READY!", t(11), t(21));
		}
	}
}