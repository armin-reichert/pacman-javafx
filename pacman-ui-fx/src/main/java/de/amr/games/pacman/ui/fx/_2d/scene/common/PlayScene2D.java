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

import static de.amr.games.pacman.model.world.World.t;
import static de.amr.games.pacman.ui.fx.util.Animations.afterSeconds;

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private Maze2D maze2D;
	private LivesCounter2D livesCounter2D;
	private Player2D player2D;
	private List<Ghost2D> ghosts2D;
	private Bonus2D bonus2D;

	public PlayScene2D(PacManGameUI_JavaFX ui, Rendering2D rendering, SoundManager sounds) {
		super(ui, rendering, sounds);
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);

		maze2D = new Maze2D(0, t(3), game, r2D);
		livesCounter2D = new LivesCounter2D(t(2), t(34), game, r2D);
		player2D = new Player2D(game.player, r2D);
		player2D.dyingAnimation.onStart(game::hideGhosts);
		ghosts2D = List.of( //
				new Ghost2D(game.ghosts[0], r2D), //
				new Ghost2D(game.ghosts[1], r2D), //
				new Ghost2D(game.ghosts[2], r2D), //
				new Ghost2D(game.ghosts[3], r2D));
		bonus2D = new Bonus2D(game.bonus, r2D, gameController.gameVariant == GameVariant.MS_PACMAN);
		game.player.powerTimer.addEventListener(this::handleGhostsFlashing);
		sounds.setMuted(gameController.attractMode);
	}

	@Override
	public void end() {
		game.player.powerTimer.removeEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void doUpdate() {
		if (gameController.currentStateID == GameState.HUNTING) {
			// ensure animations are running when switching between 2D and 3D
			if (!player2D.munchingAnimations.get(game.player.dir()).isRunning()) {
				player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			}
			if (!maze2D.getEnergizerAnimation().isRunning()) {
				maze2D.getEnergizerAnimation().restart();
			}
			AudioClip munching = sounds.getClip(GameSounds.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game.player.starvingTicks > 10) {
					sounds.stop(GameSounds.PACMAN_MUNCH);
				}
			}
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(GameSounds.SIRENS.get(e.scatterPhase - 1));
		}
		GameSounds siren = GameSounds.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying()) {
			sounds.loop(siren, Integer.MAX_VALUE);
		}
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		sounds.stop(GameSounds.PACMAN_POWER);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		ghosts2D.stream().filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
			ghost2D.flashingAnimation.reset();
			ghost2D.frightenedAnimation.restart();
		});
		if (!sounds.getClip(GameSounds.PACMAN_POWER).isPlaying()) {
			sounds.loop(GameSounds.PACMAN_POWER, Integer.MAX_VALUE);
		}
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		if (!sounds.getClip(GameSounds.PACMAN_MUNCH).isPlaying()) {
			sounds.loop(GameSounds.PACMAN_MUNCH, Integer.MAX_VALUE);
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus2D.animation.ifPresent(TimedSequence::restart);
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus2D.animation.ifPresent(TimedSequence::stop);
		sounds.play(GameSounds.BONUS_EATEN);
	}

	@Override
	public void onExtraLife(GameEvent e) {
		sounds.play(GameSounds.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		sounds.play(GameSounds.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(GameSounds.GHOST_RETURNING);
		}
	}

	@Override
	public void onPacManGameStateChange(GameStateChangeEvent e) {

		// enter READY
		if (e.newGameState == GameState.READY) {
			sounds.stopAll();
			maze2D.getEnergizerAnimation().reset();
			player2D.reset();
			ghosts2D.forEach(Ghost2D::reset);
			if (!gameController.attractMode && !gameController.gameRunning) {
				sounds.setMuted(false);
				sounds.play(GameSounds.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == GameState.HUNTING) {
			maze2D.getEnergizerAnimation().restart();
			player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		else if (e.newGameState == GameState.PACMAN_DYING) {
			// wait until game is continued
			gameController.stateTimer().setIndefinite().start();

			sounds.stopAll();

			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::reset));
			new SequentialTransition( //
					afterSeconds(1, () -> game.ghosts().forEach(Ghost::hide)), //
					afterSeconds(1, () -> {
						sounds.play(GameSounds.PACMAN_DEATH);
						player2D.dyingAnimation.restart();
					}), //
					afterSeconds(2, () -> game.player.hide()), //
					afterSeconds(1, () -> continueGame()) //
			).play();
		}

		// enter GHOST_DYING
		else if (e.newGameState == GameState.GHOST_DYING) {
			game.player.hide();
			sounds.play(GameSounds.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == GameState.LEVEL_COMPLETE) {
			gameController.stateTimer().setIndefinite(); // wait until continueGame() is called
			sounds.stopAll();
			player2D.reset();
//			game.hideGhosts();
			// Energizers can still exist if "next level" cheat has been used
			maze2D.getEnergizerAnimation().reset();
			Animation animation = new SequentialTransition( //
					maze2D.getFlashingAnimation(), //
					afterSeconds(1, this::continueGame) //
			);
			animation.setDelay(Duration.seconds(2));
			animation.play();
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == GameState.LEVEL_STARTING) {
			maze2D.setGame(game);
			gameController.stateTimer().setSeconds(1).start();
		}

		// enter GAME_OVER
		else if (e.newGameState == GameState.GAME_OVER) {
			maze2D.getEnergizerAnimation().reset();
			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
			sounds.stopAll();
		}

		// exit GHOST_DYING
		if (e.oldGameState == GameState.GHOST_DYING) {
			game.player.show();
		}
	}

	// TODO simplify
	public void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D ghost2D = ghosts2D.get(ghost.id);
				TimedSequence<?> flashing = ghost2D.flashingAnimation;
				long frameTime = e.ticks / (game.numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(game.numFlashes).restart();
			});
		}
	}

	@Override
	public void doRender() {
		if (gameController.attractMode) {
			score2D.showPoints = false;
		} else {
			score2D.showPoints = true;
			livesCounter2D.render(gc);
			renderLevelCounter();
		}
		renderGameState();
		game.ghosts(GhostState.LOCKED)
				.forEach(ghost -> ghosts2D.get(ghost.id).setLooksFrightened(game.player.powerTimer.isRunning()));
		Stream.concat(Stream.of(score2D, highScore2D, maze2D, bonus2D, player2D), ghosts2D.stream())
				.forEach(r -> r.render(gc));
	}

	private void renderGameState() {
		var state = gameController.attractMode ? GameState.GAME_OVER : gameController.currentStateID;
		if (state == GameState.GAME_OVER) {
			gc.setFont(r2D.getScoreFont());
			gc.setFill(Color.RED);
			gc.fillText("GAME", t(9), t(21));
			gc.fillText("OVER", t(15), t(21));
		} else if (state == GameState.READY) {
			gc.setFont(r2D.getScoreFont());
			gc.setFill(Color.YELLOW);
			gc.fillText("READY!", t(11), t(21));
		}
	}
}