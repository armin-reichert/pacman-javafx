package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
import de.amr.games.pacman.controller.event.PacManGameEventFacade;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.PacManLosingPowerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Controls sound and animations for the 3D play scenes.
 * 
 * @author Armin Reichert
 */
public class PlayScene3DAnimationController implements PacManGameEventFacade {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF" };

	private final PlayScene3D playScene;
	private final SoundManager sounds;
	private PacManGameController gameController;
	private List<ScaleTransition> energizerAnimations;

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

	public void init() {
		PacManGameWorld world = game().currentLevel.world;
		energizerAnimations = playScene.foodNodes.stream()//
				.filter(foodNode -> isEnergizerNode(foodNode, world))//
				.map(this::createEnergizerAnimation)//
				.collect(Collectors.toList());
	}

	private boolean isEnergizerNode(Node node, PacManGameWorld world) {
		V2i tile = (V2i) node.getUserData();
		return world.isEnergizerTile(tile);
	}

	private ScaleTransition createEnergizerAnimation(Node energizer) {
		ScaleTransition animation = new ScaleTransition(Duration.seconds(0.25), energizer);
		animation.setAutoReverse(true);
		animation.setCycleCount(Transition.INDEFINITE);
		animation.setFromX(0.2);
		animation.setFromY(0.2);
		animation.setFromZ(0.2);
		animation.setToX(1);
		animation.setToY(1);
		animation.setToZ(1);
		return animation;
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

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		boolean attractMode = gameController.isAttractMode();
		sounds.setMuted(attractMode);
		if (!attractMode) {
			PacManGameEventFacade.super.onGameEvent(gameEvent);
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
		}
		PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying())
			sounds.loop(siren, Integer.MAX_VALUE);
	}

	@Override
	public void onPacManGainsPower(PacManGainsPowerEvent e) {
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		playScene.ghosts3D.values().forEach(ghost3D -> {
			ghost3D.setBlueSkin(true);
			ghost3D.setFlashing(false);
		});
	}

	@Override
	public void onPacManLosingPower(PacManLosingPowerEvent e) {
		playScene.ghosts3D.values().forEach(ghost3D -> {
			ghost3D.setFlashing(true);
		});
	}

	@Override
	public void onPacManLostPower(PacManLostPowerEvent e) {
		playScene.ghosts3D.values().forEach(ghost3D -> {
			ghost3D.setFlashing(false);
			ghost3D.setBlueSkin(false);
		});
		sounds.stop(PacManGameSound.PACMAN_POWER);
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
	public void onBonusActivated(BonusActivatedEvent e) {
		playScene.bonus3D.showSymbol(game().bonus);
	}

	@Override
	public void onBonusEaten(BonusEatenEvent e) {
		playScene.bonus3D.showPoints(game().bonus);
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(BonusExpiredEvent e) {
		playScene.bonus3D.hide();
	}

	@Override
	public void onExtraLife(ExtraLifeEvent e) {
		gameController.getUI().showFlashMessage("Extra life!");
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturningHome(GhostReturningHomeEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
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
			gameController.stateTimer().start();
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			startEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			playAnimationPlayerDying();
			playScene.ghosts3D.values().forEach(ghost3D -> {
				ghost3D.setFlashing(false);
				ghost3D.setBlueSkin(false);
			});
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
			playScene.ghosts3D.values().forEach(ghost3D -> {
				ghost3D.setFlashing(false);
				ghost3D.setBlueSkin(false);
			});
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
		energizerAnimations.forEach(Animation::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(animation -> {
			animation.stop();
			Node energizer = animation.getNode();
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
			game().player.setDir(Direction.DOWN);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		TranslateTransition raise = new TranslateTransition(Duration.seconds(0.5), playScene.player);
		raise.setFromZ(0);
		raise.setToZ(-10);
		raise.setByZ(1);

		ScaleTransition expand = new ScaleTransition(Duration.seconds(0.5), playScene.player);
		expand.setToX(3);
		expand.setToY(3);
		expand.setToZ(3);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1), playScene.player);
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);

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