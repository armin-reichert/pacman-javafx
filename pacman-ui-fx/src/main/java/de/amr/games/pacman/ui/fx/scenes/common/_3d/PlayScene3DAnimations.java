package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.TrashTalk;
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
class PlayScene3DAnimations implements DefaultPacManGameEventHandler {

	private static V2i tile(Node node) {
		return (V2i) node.getUserData();
	}

	private final PlayScene3D playScene;
	private final SoundManager sounds;
	private List<ScaleTransition> energizerAnimations;

	public PlayScene3DAnimations(PlayScene3D playScene, SoundManager sounds) {
		this.playScene = playScene;
		this.sounds = sounds;
	}

	private PacManGameController gameController() {
		return playScene.getGameController();
	}

	private GameModel game() {
		return gameController().game();
	}

	public void buildMaze() {
		playScene.maze.init(game(), PlayScene3D.WALL_HEIGHT);
		PacManGameWorld world = game().currentLevel().world;
		energizerAnimations = playScene.maze.foodNodes()//
				.filter(foodNode -> world.isEnergizerTile(tile(foodNode)))//
				.map(this::createEnergizerAnimation)//
				.collect(Collectors.toList());
	}

	public void update() {
		if (gameController().isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameController().state == PacManGameState.HUNTING) {
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
		boolean attractMode = gameController().isAttractMode();
		sounds.setMuted(attractMode);
		DefaultPacManGameEventHandler.super.onGameEvent(gameEvent);
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
		playScene.ghosts3D.values().forEach(ghost3D -> {
			ghost3D.setBlueSkinColor();
		});
	}

	@Override
	public void onPlayerLosingPower(PacManGameEvent e) {
		playScene.ghosts3D.values().forEach(ghost3D -> {
			if (ghost3D.ghost.is(GhostState.FRIGHTENED)) {
				ghost3D.startFlashing();
			}
		});
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		playScene.ghosts3D.values().forEach(ghost3D -> {
			ghost3D.stopFlashing();
		});
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		if (e.tile.isEmpty()) {
			// this is triggered by the "eat all pellets except energizers" cheat
			Predicate<Node> isEnergizer = node -> {
				V2i tile = (V2i) node.getUserData();
				return game().currentLevel().world.isEnergizerTile(tile);
			};
			playScene.maze.foodNodes().filter(not(isEnergizer)).forEach(foodNode -> foodNode.setVisible(false));
			return;
		}
		playScene.maze.foodNodes().filter(node -> {
			V2i tile = (V2i) node.getUserData();
			return tile.equals(e.tile.get());
		}).findFirst().ifPresent(foodNode -> foodNode.setVisible(false));
		AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
		if (!munching.isPlaying()) {
			sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			Logging.log("Munching sound clip %s started", munching);
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		playScene.bonus3D.showSymbol(game().bonus());
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		playScene.bonus3D.showPoints(game().bonus());
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(PacManGameEvent e) {
		playScene.bonus3D.hide();
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
		gameController().getUI().showFlashMessage("Extra life!");
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
	public void onGhostLeavesHouse(PacManGameEvent e) {
		playScene.ghosts3D.get(e.ghost.get()).setNormalSkinColor();
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController().isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			if (!gameController().isAttractMode() && !gameController().isGameRunning()) {
				gameController().stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController().stateTimer().resetSeconds(2);
			}
			gameController().stateTimer().start();
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
				ghost3D.stopFlashing();
			});
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			buildMaze();
			playScene.levelCounter3D.update(e.gameModel);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			playAnimationLevelComplete();
			playScene.ghosts3D.values().forEach(ghost3D -> {
				ghost3D.stopFlashing();
			});
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
			gameController().getUI().showFlashMessage(TrashTalk.GAME_OVER_SPELLS.nextSpell(), 3);
		}

		// exit HUNTING but not GAME_OVER
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			playScene.bonus3D.hide();
		}
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

		double savedTranslateX = playScene.player3D.getTranslateX();
		double savedTranslateY = playScene.player3D.getTranslateY();
		double savedTranslateZ = playScene.player3D.getTranslateZ();

		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			game().player().setDir(Direction.DOWN);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		TranslateTransition raise = new TranslateTransition(Duration.seconds(0.5), playScene.player3D);
		raise.setFromZ(0);
		raise.setToZ(-10);
		raise.setByZ(1);

		ScaleTransition expand = new ScaleTransition(Duration.seconds(0.5), playScene.player3D);
		expand.setToX(3);
		expand.setToY(3);
		expand.setToZ(3);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1), playScene.player3D);
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);

		SequentialTransition animation = new SequentialTransition(phase1, raise, expand, shrink);
		animation.setOnFinished(e -> {
			playScene.player3D.setScaleX(1);
			playScene.player3D.setScaleY(1);
			playScene.player3D.setScaleZ(1);
			playScene.player3D.setTranslateX(savedTranslateX);
			playScene.player3D.setTranslateY(savedTranslateY);
			playScene.player3D.setTranslateZ(savedTranslateZ);
			game().player().visible = false;
			gameController().stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController().stateTimer().reset();
		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setDelay(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().player().visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
			String levelCompleteMessage = String.format("%s\n\nLevel %d complete.",
					TrashTalk.LEVEL_COMPLETE_SPELLS.nextSpell(), game().currentLevel().number);
			gameController().getUI().showFlashMessage(levelCompleteMessage, 2);
		});
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController().stateTimer().forceExpiration());
		animation.play();
	}

	private void playAnimationLevelStarting() {
		gameController().stateTimer().reset();
		gameController().getUI().showFlashMessage("Entering Level " + game().currentLevel().number);
		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player().visible = true;
			game().ghosts().forEach(ghost -> ghost.visible = true);
		});
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController().stateTimer().forceExpiration());
		animation.play();
	}
}