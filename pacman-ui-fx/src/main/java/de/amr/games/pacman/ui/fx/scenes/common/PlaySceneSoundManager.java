package de.amr.games.pacman.ui.fx.scenes.common;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.DeadGhostCountChangeEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Controls sound for 2D and 3D play scenes.
 * 
 * @author Armin Reichert
 */
public class PlaySceneSoundManager {

	private final PacManGameController gameController;
	private final SoundManager sounds;

	public PlaySceneSoundManager(PacManGameController gameController, SoundManager sounds) {
		this.gameController = gameController;
		this.sounds = sounds;
	}

	public void onUpdate() {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		AbstractGameModel gameModel = gameController.game();
		if (gameController.state == PacManGameState.HUNTING) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (gameModel.player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
			if (gameModel.ghosts(GhostState.DEAD).count() > 0
					&& !sounds.getClip(PacManGameSound.GHOST_RETURNING_HOME).isPlaying()) {
				sounds.loop(PacManGameSound.GHOST_RETURNING_HOME, Integer.MAX_VALUE);
			}
		}
	}

	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (newState == PacManGameState.READY) {
			sounds.stopAll();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			// start sound after 2 seconds
			PauseTransition delay = new PauseTransition(Duration.seconds(2));
			delay.setOnFinished(e -> sounds.play(PacManGameSound.PACMAN_DEATH));
			delay.play();
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
		}
	}

	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
			}
			PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
			if (!sounds.getClip(siren).isPlaying())
				sounds.loop(siren, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof PacManGainsPowerEvent) {
			sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManFoundFoodEvent) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
				Logging.log("Munching sound clip %s started", munching);
			}
		} else if (gameEvent instanceof BonusEatenEvent) {
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
}