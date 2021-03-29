package de.amr.games.pacman.ui.fx.scenes.common.scene2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.AbstractGameRendering;
import de.amr.games.pacman.ui.fx.scenes.common.PlaySceneSoundManager;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private PlaySceneSoundManager playSceneSounds;
	private LevelCompleteAnimation levelCompleteAnimation;

	public PlayScene2D(AbstractGameRendering rendering, SoundManager sounds) {
		super(rendering, sounds);
	}

	@Override
	public void setController(PacManGameController gameController) {
		super.setController(gameController);
		playSceneSounds = new PlaySceneSoundManager(gameController, sounds);
	}

	@Override
	public void start() {
		super.start();
		AbstractGameModel game = gameController.game();
		// TODO find simpler solution
		game.player.powerTimer.addEventListener(e -> {
			if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
				game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
					TimedSequence<?> flashing = rendering.ghostAnimations().ghostFlashing(ghost);
					long frameTime = e.ticks / (game.currentLevel.numFlashes * flashing.numFrames());
					flashing.frameDuration(frameTime).repetitions(game.currentLevel.numFlashes).restart();
				});
			}
		});
	}

	@Override
	public void update() {
		super.update();
		render(gameController.game());
		playSceneSounds.onUpdate();
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		AbstractGameModel gameModel = gameController.game();
		playSceneSounds.onGameStateChange(oldState, newState);

		// enter READY
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(gameModel);
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
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			rendering.mazeAnimations().energizerBlinking().restart();
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			gameModel.ghosts().forEach(ghost -> ghost.visible = false);
			levelCompleteAnimation = new LevelCompleteAnimation(gameController, gameModel.currentLevel.numFlashes);
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
			gameModel.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSounds.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGainsPowerEvent) {
			gameEvent.gameModel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				rendering.ghostAnimations().ghostFlashing(ghost).reset();
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
			gameEvent.gameModel.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> {
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.userInterface.showFlashMessage("Extra life!");
		}
	}

	private void render(AbstractGameModel game) {
		if (levelCompleteAnimation == null || !levelCompleteAnimation.isRunning()) {
			rendering.drawMaze(gc, game.currentLevel.mazeNumber, 0, t(3), false);
			rendering.drawFoodTiles(gc, game.currentLevel.world.tiles().filter(game.currentLevel.world::isFoodTile),
					game.currentLevel::containsEatenFood);
			rendering.drawEnergizerTiles(gc, game.currentLevel.world.energizerTiles());
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

	private void playAnimationPlayerDying() {
		AbstractGameModel game = gameController.game();
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		rendering.playerAnimations().playerDying().delay(120).onStart(() -> {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}).restart();
	}
}