package de.amr.games.pacman.ui.fx.scenes.common._2d;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusStateChangeEvent;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.GhostEntersHouseEvent;
import de.amr.games.pacman.controller.event.GhostReturningHomeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Controls sound and animation for the 2D play scenes.
 * 
 * @author Armin Reichert
 */
public class PlayScene2DAnimationController implements DefaultPacManGameEventHandler {

	public final SoundManager sounds;
	private final PlayScene2D<?> playScene;
	private PacManGameController gameController;
	private SequentialTransition levelCompleteAnimation;

	public PlayScene2DAnimationController(PlayScene2D<?> playScene, SoundManager sounds) {
		this.playScene = playScene;
		this.sounds = sounds;
	}

	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public void init() {
		levelCompleteAnimation = new SequentialTransition(playScene.maze2D.getFlashingAnimation(),
				new PauseTransition(Duration.seconds(1)));
		levelCompleteAnimation.setDelay(Duration.seconds(2));
		levelCompleteAnimation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
	}

	public void update() {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameController.state == PacManGameState.HUNTING) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (gameController.game().player().starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);
		DefaultPacManGameEventHandler.super.onGameEvent(gameEvent);
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
		}
		PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying()) {
			sounds.loop(siren, Integer.MAX_VALUE);
		}
	}

	@Override
	public void onPacManLostPower(PacManLostPowerEvent e) {
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPacManGainsPower(PacManGainsPowerEvent e) {
		e.gameModel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
			Ghost2D<?> ghost2D = playScene.ghosts2D.get(ghost.id);
			ghost2D.getFlashingAnimation().reset();
			ghost2D.getFrightenedAnimation().restart();
		});
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
	}

	@Override
	public void onPacManFoundFood(PacManFoundFoodEvent e) {
		AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
		if (!munching.isPlaying()) {
			sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			Logging.log("Munching sound clip %s started", munching);
		}
	}

	@Override
	public void onBonusEaten(BonusStateChangeEvent e) {
		if (playScene.bonus2D.getJumpAnimation() != null) {
			playScene.bonus2D.getJumpAnimation().reset();
		}
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onExtraLife(ExtraLifeEvent e) {
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturningHome(GhostReturningHomeEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
	}

	@Override
	public void onGhostEntersHouse(GhostEntersHouseEvent e) {
		if (gameController.game().ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			playScene.maze2D.getEnergizerBlinking().restart();
			playScene.player2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			playScene.maze2D.getEnergizerBlinking().reset();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset));
			playScene.player2D.getDyingAnimation().restart();
			sounds.stopAll();
			PauseTransition deathSound = new PauseTransition(Duration.seconds(2));
			deathSound.setOnFinished(actionEvent -> sounds.play(PacManGameSound.PACMAN_DEATH));
			deathSound.play();
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			e.gameModel.player().visible = false;
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			e.gameModel.ghosts().forEach(ghost -> ghost.visible = false);
			gameController.stateTimer().reset();
			levelCompleteAnimation.play();
			sounds.stopAll();
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			playScene.maze2D.getEnergizerBlinking().reset();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart));
			sounds.stopAll();
		}

		// exit GHOST_DYING
		if (e.oldGameState == PacManGameState.GHOST_DYING) {
			e.gameModel.player().visible = true;
		}
	}

	// TODO simplify
	public void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			gameController.game().ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D<?> ghost2D = playScene.ghosts2D.get(ghost.id);
				TimedSequence<?> flashing = ghost2D.getFlashingAnimation();
				long frameTime = e.ticks / (gameController.game().currentLevel().numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(gameController.game().currentLevel().numFlashes).restart();
			});
		}
	}
}