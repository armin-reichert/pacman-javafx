package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.GhostReturningHomeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Controls sound and animations for the 3D play scenes.
 * 
 * @author Armin Reichert
 */
public class PlayScene3DAnimationController {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF" };

	public final SoundManager sounds;
	private final PlayScene3D playScene;
	private PacManGameController gameController;

	private List<Transition> energizerAnimations;

	public PlayScene3DAnimationController(PlayScene3D playScene, SoundManager sounds) {
		this.playScene = playScene;
		this.sounds = sounds;
	}

	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	private AbstractGameModel game() {
		return gameController.game();
	}

	public void update() {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameController.state == PacManGameState.HUNTING) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game().player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
		}
	}

	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}

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
		}

		else if (gameEvent instanceof BonusActivatedEvent) {
			playScene.bonus3D.showSymbol(game().bonus);
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			playScene.bonus3D.showPoints(game().bonus);
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof BonusExpiredEvent) {
			playScene.bonus3D.hide();
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.getUI().showFlashMessage("Extra life!");
			sounds.play(PacManGameSound.EXTRA_LIFE);
		}

		else if (gameEvent instanceof GhostReturningHomeEvent) {
			sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}

	private void onGameStateChange(PacManGameStateChangedEvent e) {
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
			startEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			playScene.levelCounter3D.update(e.gameModel);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			playAnimationLevelComplete();
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
		}

		// exit HUNTING but not GAME_OVER
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			playScene.bonus3D.hide();
		}
	}

	private void startEnergizerAnimations() {
		if (energizerAnimations == null) {
			energizerAnimations = new ArrayList<>();
			playScene.energizers.forEach(energizer -> {
				ScaleTransition pumping = new ScaleTransition(Duration.seconds(0.25), energizer);
				pumping.setAutoReverse(true);
				pumping.setCycleCount(Transition.INDEFINITE);
				pumping.setFromX(0.2);
				pumping.setFromY(0.2);
				pumping.setFromZ(0.2);
				pumping.setToX(1);
				pumping.setToY(1);
				pumping.setToZ(1);
				energizerAnimations.add(pumping);
			});
		}
		energizerAnimations.forEach(Transition::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(Transition::stop);
		playScene.energizers.forEach(energizer -> {
			energizer.setScaleX(1);
			energizer.setScaleY(1);
			energizer.setScaleZ(1);
		});
	}

	private void playAnimationPlayerDying() {

		double savedTranslateX = playScene.player.getTranslateX();
		double savedTranslateY = playScene.player.getTranslateY();
		double savedTranslateZ = playScene.player.getTranslateZ();

		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			game().player.turnBothTo(Direction.DOWN);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		TranslateTransition raise = new TranslateTransition(Duration.seconds(0.5), playScene.player);
		raise.setFromZ(0);
		raise.setToZ(-10);
		raise.setByZ(1);

		ScaleTransition expand = new ScaleTransition(Duration.seconds(0.5), playScene.player);
		expand.setToX(2);
		expand.setToY(2);
		expand.setToZ(2);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1), playScene.player);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		SequentialTransition animation = new SequentialTransition(phase1, raise, expand, shrink);
		animation.setOnFinished(e -> {
			playScene.player.setScaleX(1);
			playScene.player.setScaleY(1);
			playScene.player.setScaleZ(1);
			playScene.player.setTranslateX(savedTranslateX);
			playScene.player.setTranslateY(savedTranslateY);
			playScene.player.setTranslateZ(savedTranslateZ);
			game().player.visible = false;
			gameController.stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();
		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setDelay(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().player.visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
			String congrats = CONGRATS[new Random().nextInt(CONGRATS.length)];
			String message = String.format("%s!\n\nLevel %d complete.", congrats, game().currentLevelNumber);
			gameController.getUI().showFlashMessage(message, 2);
		});
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}

	private void playAnimationLevelStarting() {
		gameController.stateTimer().reset();
		gameController.getUI().showFlashMessage("Entering Level " + gameController.game().currentLevelNumber);
		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = true;
			game().ghosts().forEach(ghost -> ghost.visible = true);
		});
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}