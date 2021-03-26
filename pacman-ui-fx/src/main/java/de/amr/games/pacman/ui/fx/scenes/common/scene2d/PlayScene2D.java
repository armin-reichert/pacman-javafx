package de.amr.games.pacman.ui.fx.scenes.common.scene2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.DeadGhostCountChangeEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private LevelCompleteAnimation levelCompleteAnimation;

	public PlayScene2D(PacManGameRendering2D rendering, SoundManager sounds) {
		super(rendering, sounds);
	}

	private void playAnimationPlayerDying() {
		GameModel game = gameController.game();
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		rendering.playerAnimations().playerDying().delay(120).onStart(() -> {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			if (gameController.isGameRunning()) {
				sounds.play(PacManGameSound.PACMAN_DEATH);
			}
		}).restart();
	}

	@Override
	public void start() {
		super.start();
		GameModel game = gameController.game();
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
		GameModel gameModel = gameController.game();

		// enter READY
		if (newState == PacManGameState.READY) {
			sounds.stopAll();
			rendering.resetAllAnimations(gameModel);
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter HUNTING
		if (newState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().restart();
			rendering.playerAnimations().playerMunching(gameModel.player).forEach(TimedSequence::restart);
			gameController.game().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::restart);
		}

		// exit HUNTING
		if (oldState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().reset();
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
			rendering.mazeAnimations().energizerBlinking().restart();
		}

		// exit GHOST_DYING
		if (oldState == PacManGameState.GHOST_DYING) {
			// the dead(s) ghost will return home now
			if (gameModel.ghosts(GhostState.DEAD).count() > 0) {
				sounds.loop(PacManGameSound.GHOST_RETURNING_HOME, Integer.MAX_VALUE);
			}
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			gameModel.ghosts().forEach(ghost -> ghost.visible = false);
			levelCompleteAnimation = new LevelCompleteAnimation(gameController, gameModel.level.numFlashes);
			double totalDuration = levelCompleteAnimation.getTotalDuration().toSeconds();
			log("Total LEVEL_COMPLETE animation duration: %f", totalDuration);
			gameController.stateTimer().resetSeconds(totalDuration);
			levelCompleteAnimation.play();
		}

		// enter LEVEL_STARTING
		if (newState == PacManGameState.LEVEL_STARTING) {
			gameController.stateTimer().reset(1);
			gameController.stateTimer().start();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
			gameModel.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
			}
			sounds.loop(PacManGameSound.SIRENS.get(e.scatterPhase), Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof PacManFoundFoodEvent) {
			sounds.play(PacManGameSound.PACMAN_MUNCH);
		}

		else if (gameEvent instanceof PacManGainsPowerEvent) {
			sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
			gameEvent.gameModel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				rendering.ghostAnimations().ghostFlashing(ghost).reset();
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
			gameEvent.gameModel.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> {
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			sounds.play(PacManGameSound.EXTRA_LIFE);
			gameController.userInterface.showFlashMessage("Extra life!");
		}

		else if (gameEvent instanceof DeadGhostCountChangeEvent) {
			DeadGhostCountChangeEvent e = (DeadGhostCountChangeEvent) gameEvent;
			if (e.oldCount == 0 && e.newCount > 0) {
				sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
			} else if (e.oldCount > 0 && e.newCount == 0) {
				sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
			}
		}
	}

	@Override
	public void update() {
		super.update();
		render(gameController.game());
	}

	private void render(GameModel game) {
		if (levelCompleteAnimation == null || !levelCompleteAnimation.isRunning()) {
			rendering.drawMaze(gc, game.level.mazeNumber, 0, t(3), false);
			rendering.drawFoodTiles(gc, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(gc, game.level.world.energizerTiles());
		} else {
			gc.drawImage(levelCompleteAnimation.getCurrentMazeImage(), 0, t(3));
		}
		if (gameController.isGameRunning()
				|| gameController.state == PacManGameState.READY && !gameController.isAttractMode()) {
			rendering.drawLivesCounter(gc, game, t(2), t(34));
			rendering.drawGameState(gc, game, gameController.state);
			rendering.drawScore(gc, game, false);
		} else {
			rendering.drawGameState(gc, game, PacManGameState.GAME_OVER);
			rendering.drawScore(gc, game, true);
		}
		rendering.drawBonus(gc, game.bonus);
		rendering.drawPlayer(gc, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(gc, ghost, game.player.powerTimer.isRunning()));
		rendering.drawLevelCounter(gc, game, t(25), t(34));
	}
}