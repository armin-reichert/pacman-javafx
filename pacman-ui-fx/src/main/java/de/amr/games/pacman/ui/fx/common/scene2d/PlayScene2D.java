package de.amr.games.pacman.ui.fx.common.scene2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.Camera;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private TimedSequence<?> mazeFlashing;

	public PlayScene2D(PacManGameController controller, PacManGameRendering2D rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.of(aspectRatio);
	}

	@Override
	public Camera getActiveCamera() {
		return scene.getCamera();
	}

	@Override
	public void useMoveableCamera(boolean use) {
		// 2D scene has only static camera
	}

	private void startPlayerDyingAnimation(PacManGameState state) {
		GameModel game = controller.selectedGame();
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		game.ghosts().forEach(ghost -> ghost.visible = false);
		rendering.playerAnimations().playerDying().restart();
		sounds.play(PacManGameSound.PACMAN_DEATH);
	}

	// TODO use FX animation
	private void runLevelCompleteState(PacManGameState state) {
		GameModel game = controller.selectedGame();
		if (state.timer.isRunningSeconds(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (state.timer.isRunningSeconds(3)) {
			mazeFlashing.restart();
		}
		mazeFlashing.animate();
		if (mazeFlashing.isComplete()) {
			controller.letCurrentGameStateExpire();
		}
	}

	@Override
	public void start() {
		GameModel game = controller.selectedGame();
		mazeFlashing = rendering.mazeAnimations().mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes);
		mazeFlashing.reset();
		game.player.powerTimer.addEventListener(e -> {
			if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
				game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
					TimedSequence<?> flashing = rendering.ghostAnimations().ghostFlashing(ghost);
					long frameTime = e.ticks / (game.level.numFlashes * flashing.numFrames());
					flashing.frameDuration(frameTime).repetitions(game.level.numFlashes).restart();
				});
			}
		});
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		// enter READY state
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(controller.selectedGame());
		}

		// enter HUNTING state
		if (newState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().restart();
			rendering.playerAnimations().playerMunching(controller.selectedGame().player).forEach(TimedSequence::restart);
			controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking)
					.forEach(TimedSequence::restart);
		}

		// exit HUNTING state
		if (oldState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().reset();
		}

		// enter PAC_MANDYING state
		if (newState == PacManGameState.PACMAN_DYING) {
			startPlayerDyingAnimation(newState);
		}

		// enter GHOST_DYING state
		if (newState == PacManGameState.GHOST_DYING) {
			rendering.mazeAnimations().energizerBlinking().restart();
		}

		// enter LEVEL_COMPLETE state
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			mazeFlashing = rendering.mazeAnimations().mazeFlashing(controller.selectedGame().level.mazeNumber);
		}

		// enter GAME_OVER state
		if (newState == PacManGameState.GAME_OVER) {
			controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking)
					.forEach(TimedSequence::reset);
		}
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		if (controller.state == PacManGameState.LEVEL_COMPLETE) {
			runLevelCompleteState(controller.state);
		} else if (controller.state == PacManGameState.LEVEL_STARTING) {
			controller.letCurrentGameStateExpire();
		}
		render();
	}

	private void render() {
		GameModel game = controller.selectedGame();
		rendering.drawMaze(gc, game.level.mazeNumber, 0, t(3), mazeFlashing.isRunning());
		if (!mazeFlashing.isRunning()) {
			rendering.drawFoodTiles(gc, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(gc, game.level.world.energizerTiles());
		}
		if (controller.isAttractMode()) {
			rendering.drawGameState(gc, game, PacManGameState.GAME_OVER);
		} else {
			rendering.drawGameState(gc, game, controller.state);
		}
		rendering.drawBonus(gc, game.bonus);
		rendering.drawPlayer(gc, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(gc, ghost, game.player.powerTimer.isRunning()));
		rendering.drawScore(gc, game, controller.state == PacManGameState.INTRO || controller.isAttractMode());
		if (!controller.isAttractMode()) {
			rendering.drawLivesCounter(gc, game, t(2), t(34));
		}
		rendering.drawLevelCounter(gc, game, t(25), t(34));
	}
}