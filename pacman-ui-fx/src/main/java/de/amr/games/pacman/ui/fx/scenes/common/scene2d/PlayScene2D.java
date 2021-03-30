package de.amr.games.pacman.ui.fx.scenes.common.scene2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.Bonus2D;
import de.amr.games.pacman.ui.fx.rendering.Energizer2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.Ghost2D;
import de.amr.games.pacman.ui.fx.rendering.Player2D;
import de.amr.games.pacman.ui.fx.scenes.common.PlaySceneSoundManager;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private Player2D player2D;
	private List<Ghost2D> ghosts2D;
	private List<Energizer2D> energizers2D;
	private Bonus2D bonus2D;

	private PlaySceneSoundManager playSceneSounds;
	private LevelCompleteAnimation levelCompleteAnimation;

	public PlayScene2D(GameRendering2D rendering, SoundManager sounds) {
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

		player2D = new Player2D(game().player);
		player2D.setRendering(rendering);

		ghosts2D = game().ghosts().map(Ghost2D::new).collect(Collectors.toList());
		ghosts2D.forEach(ghost2D -> ghost2D.setRendering(rendering));

		energizers2D = game().currentLevel.world.energizerTiles().map(Energizer2D::new).collect(Collectors.toList());

		bonus2D = new Bonus2D();
		bonus2D.setRendering(rendering);

		game().player.powerTimer.addEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void end() {
		game().player.powerTimer.removeEventListener(this::handleGhostsFlashing);
		super.end();
	}

	@Override
	public void update() {
		super.update();
		render();
		playSceneSounds.onUpdate();
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		playSceneSounds.onGameStateChange(oldState, newState);

		// enter READY
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(game());
		}

		// enter HUNTING
		if (newState == PacManGameState.HUNTING) {
			energizers2D.forEach(energizer2D -> energizer2D.getBlinkingAnimation().restart());
			player2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
			ghosts2D.forEach(ghost2D -> {
				ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart);
			});
		}

		// exit HUNTING
		if (oldState == PacManGameState.HUNTING) {
			energizers2D.forEach(energizer2D -> energizer2D.getBlinkingAnimation().reset());
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			energizers2D.forEach(energizer2D -> energizer2D.getBlinkingAnimation().restart());
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			levelCompleteAnimation = new LevelCompleteAnimation(gameController, game().currentLevel.numFlashes);
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
			ghosts2D.forEach(ghost2D -> {
				ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset);
			});
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSounds.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGainsPowerEvent) {
			ghosts2D.stream().filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
				ghost2D.getFlashingAnimation().reset();
				ghost2D.getFrightenedAnimation().restart();
			});
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			if (bonus2D.getJumpAnimation() != null) {
				bonus2D.getJumpAnimation().reset();
			}
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.userInterface.showFlashMessage("Extra life!");
		}
	}

	private void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			ghosts2D.stream().filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
				TimedSequence<?> flashing = ghost2D.getFlashingAnimation();
				long frameTime = e.ticks / (game().currentLevel.numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(game().currentLevel.numFlashes).restart();
			});
		}
	}

	private void playAnimationPlayerDying() {
		ghosts2D.forEach(ghost2D -> {
			ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset);
		});
		player2D.getDyingAnimation().delay(120).onStart(() -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			if (gameController.isGameRunning()) {
				sounds.play(PacManGameSound.PACMAN_DEATH);
			}
		}).restart();
	}

	private void render() {
		if (levelCompleteAnimation == null || !levelCompleteAnimation.isRunning()) {
			rendering.drawMaze(gc, game().currentLevel.mazeNumber, 0, t(3), false);
			rendering.drawFoodTiles(gc, game().currentLevel.world.tiles().filter(game().currentLevel.world::isFoodTile),
					game().currentLevel::containsEatenFood);
			energizers2D.forEach(energizer2D -> energizer2D.render(gc));
		} else {
			gc.drawImage(levelCompleteAnimation.getCurrentMazeImage(), 0, t(3));
		}
		if (gameController.isAttractMode()) {
			rendering.drawGameState(gc, game(), PacManGameState.GAME_OVER);
		} else {
			rendering.drawGameState(gc, game(), gameController.state);
		}
		bonus2D.render(gc);
		player2D.render(gc);
		ghosts2D.forEach(ghost2D -> {
			ghost2D.setDisplayFrightened(game().player.powerTimer.isRunning());
			ghost2D.render(gc);
		});
		if (gameController.isGameRunning()) {
			rendering.drawScore(gc, game(), false);
			rendering.drawLivesCounter(gc, game(), t(2), t(34));
		} else {
			rendering.drawScore(gc, game(), true);
		}
		rendering.drawLevelCounter(gc, game(), t(25), t(34));
	}
}