/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.ui.fx.util.Animations.afterSeconds;
import static de.amr.games.pacman.ui.fx.util.Animations.now;
import static de.amr.games.pacman.ui.fx.util.Animations.pause;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.media.AudioClip;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3DWithAnimations extends PlayScene3D {

	private static ScaleTransition createEnergizerAnimation(Node energizer) {
		var animation = new ScaleTransition(Duration.seconds(0.25), energizer);
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

	private final SoundManager sounds;
	private List<Transition> energizerAnimations;

	public PlayScene3DWithAnimations(PacManModel3D model3D, SoundManager sounds) {
		super(model3D);
		this.sounds = sounds;
	}

	@Override
	protected void buildMaze(PacManGameWorld world, int mazeNumber) {
		super.buildMaze(world, mazeNumber);
		energizerAnimations = energizerNodes(world).map(PlayScene3DWithAnimations::createEnergizerAnimation)
				.collect(Collectors.toList());
	}

	@Override
	public void update() {
		super.update();
		final GameModel game = gameController.game();
		playDoorAnimation();
		sounds.setMuted(gameController.isAttractMode());
		if (gameController.currentStateID == PacManGameState.HUNTING) {
			// update food visibility and animations in case of switching between 2D and 3D view
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game.isFoodEaten(tile(foodNode)));
			});
			if (energizerAnimations.stream().anyMatch(animation -> animation.getStatus() != Status.RUNNING)) {
				energizerAnimations.forEach(Transition::play);
			}
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game.player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
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
		ghosts3D.stream().filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(Ghost3D::setBlueSkinColor);
	}

	@Override
	public void onPlayerLosingPower(PacManGameEvent e) {
		ghosts3D.stream()//
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED))//
				.forEach(ghost3D -> ghost3D.playFlashingAnimation());
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		ghosts3D.forEach(ghost3D -> ghost3D.setNormalSkinColor());
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		if (e.tile.isEmpty()) {
			// this happens when the "eat all pellets except energizers" cheat was triggered
			Predicate<Node> isEnergizer = node -> gameController.game().world.isEnergizerTile(tile(node));
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
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		bonus3D.showSymbol(gameController.game().bonus);
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		bonus3D.showPoints(gameController.game().bonus);
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(PacManGameEvent e) {
		bonus3D.hide();
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
		gameController.getUI().showFlashMessage(1, Env.message("extra_life"));
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(PacManGameEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
	}

	@Override
	public void onGhostEntersHouse(PacManGameEvent e) {
		if (gameController.game().ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}

	@Override
	public void onGhostLeavingHouse(PacManGameEvent e) {
		ghosts3D.get(e.ghost.get().id).setNormalSkinColor();
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			player3D.reset();
			resetEnergizers(gameController.game().world);
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
			ghosts3D.forEach(ghost3D -> ghost3D.setNormalSkinColor());
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			buildMaze(gameController.game().world, gameController.game().mazeNumber);
			levelCounter3D.rebuild(e.game);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			playAnimationLevelComplete();
			ghosts3D.forEach(ghost3D -> ghost3D.setNormalSkinColor());
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
			gameController.getUI().showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}

		// exit HUNTING but not GAME_OVER
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			bonus3D.hide();
		}
	}

	private Stream<Node> energizerNodes(PacManGameWorld world) {
		return maze3D.foodNodes().filter(node -> world.isEnergizerTile(tile(node)));
	}

	private void resetEnergizers(PacManGameWorld world) {
		energizerNodes(world).forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	private void playEnergizerAnimations() {
		energizerAnimations.forEach(Animation::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(Animation::stop);
	}

	private void playAnimationPlayerDying() {
		var hideGhosts = now(() -> gameController.game().ghosts().forEach(ghost -> ghost.visible = false));
		var playSound = now(() -> sounds.play(PacManGameSound.PACMAN_DEATH));
		var impale = player3D.createImpaleAnimation(Duration.seconds(1));

		var spin = new RotateTransition(Duration.seconds(0.2), player3D);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), player3D);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		var animation = new SequentialTransition(pause(0.5), hideGhosts, impale, pause(1),
				new ParallelTransition(playSound, spin, shrink), pause(2));
		animation.setOnFinished(e -> gameController.stateTimer().expire());
		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();
		gameController.stateTimer().start();
		var hideGuysAndShowMessage = afterSeconds(3, () -> {
			gameController.game().player.visible = false;
			gameController.game().ghosts().forEach(ghost -> ghost.visible = false);
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n"
					+ Env.message("level_complete", gameController.game().levelNumber);
			gameController.getUI().showFlashMessage(2, message);
		});
		var quitLevel = afterSeconds(2, () -> gameController.stateTimer().expire());
		new SequentialTransition(hideGuysAndShowMessage, quitLevel).play();
	}

	private void playAnimationLevelStarting() {
		gameController.getUI().showFlashMessage(1, Env.message("level_starting", gameController.game().levelNumber));
		var hideGuys = afterSeconds(1, () -> {
			gameController.game().player.visible = true;
			gameController.game().ghosts().forEach(ghost -> ghost.visible = true);
		});
		var startLevel = afterSeconds(3, () -> gameController.stateTimer().expire());
		new SequentialTransition(hideGuys, startLevel).play();
	}

	private void playDoorAnimation() {
		boolean open = maze3D.doors()
				.anyMatch(door -> gameController.game().ghosts().anyMatch(ghost -> ghost.tile().equals(tile(door))));
		maze3D.showDoorsOpen(open);
	}
}