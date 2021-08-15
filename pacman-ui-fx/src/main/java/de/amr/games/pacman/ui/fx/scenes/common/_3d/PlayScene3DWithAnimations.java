package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import de.amr.games.pacman.ui.fx.scenes.common.TrashTalk;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3DWithAnimations extends PlayScene3D implements DefaultPacManGameEventHandler {

	private static V2i tile(Node node) {
		return (V2i) node.getUserData();
	}

	private final SoundManager sounds;
	private List<ScaleTransition> energizerAnimations;

	public PlayScene3DWithAnimations(PacManModel3D model3D, SoundManager sounds) {
		super(model3D);
		this.sounds = sounds;
	}

	@Override
	protected void buildMaze() {
		super.buildMaze();
		energizerAnimations = energizerNodes().map(this::createEnergizerAnimation).collect(Collectors.toList());
	}

	@Override
	public void update() {
		super.update();
		sounds.setMuted(gameController.isAttractMode());
		if (gameController.state == PacManGameState.HUNTING) {
			// when switching between 2D and 3D, food visibility and animations might not be
			// up-to-date, so:
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game().level().isFoodRemoved(tile(foodNode)));
			});
			if (energizerAnimations.stream().anyMatch(animation -> animation.getStatus() != Status.RUNNING)) {
				energizerAnimations.forEach(Transition::play);
			}
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game().player().starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		sounds.setMuted(gameController.isAttractMode());
		super.onGameEvent(gameEvent);
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
	public void onPlayerGainsPower(PacManGameEvent e) {
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		ghosts3D.stream()
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(Ghost3D::setBlueSkinColor);
	}

	@Override
	public void onPlayerLosingPower(PacManGameEvent e) {
		ghosts3D.stream()//
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED))//
				.forEach(ghost3D -> ghost3D.flashingAnimation.playFromStart());
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		ghosts3D.forEach(ghost3D -> ghost3D.flashingAnimation.stop());
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		if (e.tile.isEmpty()) {
			// this happens when the "eat all pellets except energizers" cheat was triggered
			Predicate<Node> isEnergizer = node -> game().level().world.isEnergizerTile(tile(node));
			maze3D.foodNodes()//
					.filter(not(isEnergizer))//
					.forEach(foodNode -> foodNode.setVisible(false));
			return;
		}
		maze3D.foodNodes()//
				.filter(node -> tile(node).equals(e.tile.get()))//
				.findFirst()//
				.ifPresent(foodNode -> foodNode.setVisible(false));

		AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
		if (!munching.isPlaying()) {
			sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			Logging.log("Munching sound clip %s started", munching);
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		bonus3D.showSymbol(game().bonus());
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		bonus3D.showPoints(game().bonus());
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(PacManGameEvent e) {
		bonus3D.hide();
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
		String message = PacManGameUI_JavaFX.message("extra_life");
		gameController.getUI().showFlashMessage(1, message);
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(PacManGameEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
	}

	@Override
	public void onGhostEntersHouse(PacManGameEvent e) {
		if (game().ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}

	@Override
	public void onGhostLeavingHouse(PacManGameEvent e) {
		Ghost ghost = e.ghost.get();
		ghosts3D.get(ghost.id).setNormalSkinColor();
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			resetEnergizers();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				sounds.play(PacManGameSound.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			playEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			playAnimationPlayerDying();
			ghosts3D.forEach(ghost3D -> ghost3D.flashingAnimation.stop());
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			buildMaze();
			levelCounter3D.rebuild(e.gameModel);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			playAnimationLevelComplete();
			ghosts3D.forEach(ghost3D -> ghost3D.flashingAnimation.stop());
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
			gameController.getUI().showFlashMessage(3, TrashTalk.GAME_OVER_TALK.next());
		}

		// exit HUNTING but not GAME_OVER
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			bonus3D.hide();
		}
	}

	private Stream<Node> energizerNodes() {
		return maze3D.foodNodes().filter(node -> game().level().world.isEnergizerTile(tile(node)));
	}

	private void resetEnergizers() {
		energizerNodes().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	private ScaleTransition createEnergizerAnimation(Node energizer) {
		ScaleTransition animation = new ScaleTransition(Duration.seconds(0.25), energizer);
		animation.setAutoReverse(true);
		animation.setCycleCount(Transition.INDEFINITE);
		animation.setFromX(1.0);
		animation.setFromY(1.0);
		animation.setFromZ(1.0);
		animation.setToX(0.1);
		animation.setToY(0.1);
		animation.setToZ(0.1);
		return animation;
	}

	private void playEnergizerAnimations() {
		energizerAnimations.forEach(Animation::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(Animation::stop);
	}

	private void playAnimationPlayerDying() {

		double savedTranslateX = player3D.getTranslateX();
		double savedTranslateY = player3D.getTranslateY();
		double savedTranslateZ = player3D.getTranslateZ();

		PauseTransition phase1 = new PauseTransition(Duration.seconds(3));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.setVisible(false));
			game().player().setDir(Direction.DOWN);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		TranslateTransition raise = new TranslateTransition(Duration.seconds(0.5), player3D);
		raise.setFromZ(savedTranslateZ);
		raise.setToZ(-10);
		raise.setByZ(1);

		ScaleTransition expand = new ScaleTransition(Duration.seconds(0.5), player3D);
		expand.setToX(3);
		expand.setToY(3);
		expand.setToZ(3);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1), player3D);
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);
		shrink.setOnFinished(e -> {
			player3D.setVisible(false);
		});

		PauseTransition end = new PauseTransition(Duration.seconds(1));
		end.setOnFinished(e -> {
			player3D.setVisible(true);
			player3D.setScaleX(1);
			player3D.setScaleY(1);
			player3D.setScaleZ(1);
			player3D.setTranslateX(savedTranslateX);
			player3D.setTranslateY(savedTranslateY);
			player3D.setTranslateZ(savedTranslateZ);
			gameController.stateTimer().expire();
		});

		new SequentialTransition(phase1, raise, expand, shrink, end).play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();
		gameController.stateTimer().start();
		PauseTransition phase1 = new PauseTransition(Duration.seconds(3));
		phase1.setOnFinished(e -> {
			game().player().setVisible(false);
			game().ghosts().forEach(ghost -> ghost.setVisible(false));
			String message = TrashTalk.LEVEL_COMPLETE_TALK.next();
			message += "\n\n";
			message += PacManGameUI_JavaFX.message("level_complete", game().level().number);
			gameController.getUI().showFlashMessage(2, message);
		});
		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));
		phase2.setOnFinished(e -> gameController.stateTimer().expire());
		new SequentialTransition(phase1, phase2).play();
	}

	private void playAnimationLevelStarting() {
		gameController.stateTimer().reset();
		gameController.stateTimer().start();
		String message = PacManGameUI_JavaFX.message("level_starting", game().level().number);
		gameController.getUI().showFlashMessage(1, message);
		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().player().setVisible(true);
			game().ghosts().forEach(ghost -> ghost.setVisible(true));
		});
		PauseTransition phase2 = new PauseTransition(Duration.seconds(3));
		phase2.setOnFinished(e -> gameController.stateTimer().expire());
		new SequentialTransition(phase1, phase2).play();
	}
}