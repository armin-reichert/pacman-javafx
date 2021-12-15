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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
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
public class PlayScene2DWithAnimations implements DefaultPacManGameEventHandler {

	public final SoundManager sounds;
	private final PlayScene2D playScene;
	private PacManGameController gameController;
	private SequentialTransition levelCompleteAnimation;

	public PlayScene2DWithAnimations(PlayScene2D playScene, SoundManager sounds) {
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
		levelCompleteAnimation.setOnFinished(e -> gameController.stateTimer().expire());
	}

	private GameModel game() {
		return gameController.game();
	}

	public void update() {
		sounds.setMuted(gameController.isAttractMode());

		if (gameController.currentStateID == PacManGameState.HUNTING) {

			// when switching between 2D and 3D play scenes, animations might not be
			// running:
			if (!playScene.player2D.munchingAnimations.get(game().player.dir()).isRunning()) {
				playScene.player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			}
			if (!playScene.maze2D.getEnergizerAnimation().isRunning()) {
				playScene.maze2D.getEnergizerAnimation().restart();
			}

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
	public void onPlayerLostPower(PacManGameEvent e) {
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerGainsPower(PacManGameEvent e) {
		e.game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
			Ghost2D ghost2D = playScene.ghosts2D.get(ghost.id);
			ghost2D.flashingAnimation.reset();
			ghost2D.frightenedAnimation.restart();
		});
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
		if (!munching.isPlaying()) {
			sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			Logging.log("Munching sound clip %s started", munching);
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		playScene.bonus2D.startAnimation();
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		playScene.bonus2D.stopAnimation();
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
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
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			playScene.maze2D.getEnergizerAnimation().reset();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				sounds.play(PacManGameSound.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			playScene.maze2D.getEnergizerAnimation().restart();
			playScene.player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			gameController.stateTimer().resetSeconds(5);
			gameController.stateTimer().start();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::reset));
			playScene.player2D.dyingAnimation.restart();
			sounds.stopAll();
			PauseTransition deathSound = new PauseTransition(Duration.seconds(2));
			deathSound.setOnFinished(actionEvent -> sounds.play(PacManGameSound.PACMAN_DEATH));
			deathSound.play();
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			e.game.player.setVisible(false);
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			playScene.maze2D.getEnergizerAnimation().reset(); // energizers may still exist when cheat is used
			e.game.ghosts().forEach(ghost -> ghost.setVisible(false));
			gameController.stateTimer().reset();
			levelCompleteAnimation.play();
			sounds.stopAll();
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			playScene.maze2D.getEnergizerAnimation().reset();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
			sounds.stopAll();
		}

		// exit GHOST_DYING
		if (e.oldGameState == PacManGameState.GHOST_DYING) {
			e.game.player.setVisible(true);
		}
	}

	// TODO simplify
	public void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			gameController.game().ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D ghost2D = playScene.ghosts2D.get(ghost.id);
				TimedSequence<?> flashing = ghost2D.flashingAnimation;
				long frameTime = e.ticks / (gameController.game().numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(gameController.game().numFlashes).restart();
			});
		}
	}
}