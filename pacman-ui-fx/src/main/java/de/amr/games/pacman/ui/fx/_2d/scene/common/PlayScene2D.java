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
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx.util.U.afterSeconds;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
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
	private LevelCounter2D levelCounter2D;
	private Player2D player2D;
	private Ghost2D[] ghosts2D = new Ghost2D[4];
	private Bonus2D bonus2D;

	public PlayScene2D(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
	}

	@Override
	public void init() {
		super.init();

		maze2D = new Maze2D(game, r2D);
		maze2D.x = 0;
		maze2D.y = t(3);

		score2D.showPoints = !gameController.attractMode;

		livesCounter2D = new LivesCounter2D(game, r2D);
		livesCounter2D.x = t(2);
		livesCounter2D.y = t(34);
		livesCounter2D.visible = !gameController.attractMode;

		levelCounter2D = new LevelCounter2D(game, r2D);
		levelCounter2D.rightPosition = unscaledSize.minus(t(3), t(2));
		levelCounter2D.visible = !gameController.attractMode;

		player2D = new Player2D(game.player, game, r2D);
		player2D.animDying.onStart(game::hideGhosts);

		for (Ghost ghost : game.ghosts) {
			ghosts2D[ghost.id] = new Ghost2D(ghost, game, r2D);
		}

		boolean bonusMoving = gameController.gameVariant == GameVariant.MS_PACMAN;
		bonus2D = new Bonus2D(game.bonus, r2D, bonusMoving);

		game.player.powerTimer.addEventListener(this::handleGhostsFlashing);
		SoundManager.get().setMuted(gameController.attractMode);
	}

	@Override
	public void end() {
		game.player.powerTimer.removeEventListener(this::handleGhostsFlashing);
		SoundManager.get().setMuted(false);
		log("Scene '%s' ended", getClass().getName());
	}

	private void handleGhostsFlashing(TickTimerEvent e) {
		// TODO this is somewhat dubious
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			game.ghosts(GhostState.FRIGHTENED).map(ghost -> ghosts2D[ghost.id]).forEach(ghost2D -> {
				long frameTicks = e.ticks / (game.numFlashes * ghost2D.animFlashing.numFrames());
				ghost2D.animFlashing.frameDuration(frameTicks).repetitions(game.numFlashes).restart();
			});
		}
	}

	@Override
	protected void doUpdate() {
		if (SoundManager.get().getClip(GameSounds.PACMAN_MUNCH).isPlaying() && game.player.starvingTicks > 10) {
			SoundManager.get().stop(GameSounds.PACMAN_MUNCH);
		}
		int scatterPhase = game.huntingPhase % 2;
		GameSounds siren = GameSounds.SIRENS.get(scatterPhase);
		if (gameController.state == GameState.HUNTING && !SoundManager.get().getClip(siren).isPlaying()) {
			SoundManager.get().loop(siren, Animation.INDEFINITE);
		}
	}

	public void onSwitchBetween2DAnd3D() {
		if (!player2D.animMunching.get(game.player.moveDir()).isRunning()) {
			player2D.animMunching.values().forEach(TimedSeq::restart);
		}
		for (Ghost2D ghost2D : ghosts2D) {
			for (Direction dir : Direction.values()) {
				if (!ghost2D.animKicking.get(dir).isRunning()) {
					ghost2D.animKicking.get(dir).restart();
				}
			}
			if (!ghost2D.animFrightened.isRunning()) {
				ghost2D.animFrightened.restart();
			}
		}
		if (!maze2D.getEnergizerAnimation().isRunning()) {
			maze2D.getEnergizerAnimation().restart();
		}
		AudioClip munching = SoundManager.get().getClip(GameSounds.PACMAN_MUNCH);
		if (munching.isPlaying() && game.player.starvingTicks > 10) {
			SoundManager.get().stop(GameSounds.PACMAN_MUNCH);
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			SoundManager.get().stop(GameSounds.SIRENS.get(e.scatterPhase - 1));
		}
		GameSounds siren = GameSounds.SIRENS.get(e.scatterPhase);
		if (!SoundManager.get().getClip(siren).isPlaying()) {
			SoundManager.get().loop(siren, Animation.INDEFINITE);
		}
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		SoundManager.get().stop(GameSounds.PACMAN_POWER);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		Stream.of(ghosts2D).filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
			ghost2D.animFlashing.reset();
			ghost2D.animFrightened.restart();
		});
		if (!SoundManager.get().getClip(GameSounds.PACMAN_POWER).isPlaying()) {
			SoundManager.get().loop(GameSounds.PACMAN_POWER, Animation.INDEFINITE);
		}
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		if (!SoundManager.get().getClip(GameSounds.PACMAN_MUNCH).isPlaying()) {
			SoundManager.get().loop(GameSounds.PACMAN_MUNCH, Animation.INDEFINITE);
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus2D.startAnimation();
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus2D.stopAnimation();
		SoundManager.get().play(GameSounds.BONUS_EATEN);
	}

	@Override
	public void onExtraLife(GameEvent e) {
		SoundManager.get().play(GameSounds.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		SoundManager.get().play(GameSounds.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			SoundManager.get().stop(GameSounds.GHOST_RETURNING);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {

		// enter state XYZ
		switch (e.newGameState) {

		case READY -> {
			SoundManager.get().stopAll();
			maze2D.getEnergizerAnimation().reset();
			player2D.reset();
			Stream.of(ghosts2D).forEach(Ghost2D::reset);
			if (!gameController.attractMode && !gameController.gameRunning) {
				SoundManager.get().setMuted(false);
				SoundManager.get().play(GameSounds.GAME_READY);
			}
		}

		case HUNTING -> {
			maze2D.getEnergizerAnimation().restart();
			player2D.animMunching.values().forEach(TimedSeq::restart);
			Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animKicking.values().forEach(TimedSeq::restart));
		}

		case PACMAN_DYING -> {
			// wait until game is continued
			gameController.stateTimer().setIndefinite().start();

			SoundManager.get().stopAll();

			new SequentialTransition( //
					afterSeconds(1, () -> game.ghosts().forEach(Ghost::hide)), //
					afterSeconds(1, () -> {
						SoundManager.get().play(GameSounds.PACMAN_DEATH);
						player2D.animDying.restart();
					}), //
					afterSeconds(2, () -> game.player.hide()), //
					afterSeconds(1, () -> gameController.stateTimer().expire()) //
			).play();
		}

		case GHOST_DYING -> {
			game.player.hide();
			SoundManager.get().play(GameSounds.GHOST_EATEN);
		}

		case LEVEL_COMPLETE -> {
			gameController.stateTimer().setIndefinite(); // wait until continueGame() is called
			SoundManager.get().stopAll();
			player2D.reset();
			// Energizers can still exist if "next level" cheat has been used
			maze2D.getEnergizerAnimation().reset();
			Animation animation = new SequentialTransition( //
					maze2D.getFlashingAnimation(), //
					afterSeconds(1, () -> gameController.stateTimer().expire()) //
			);
			animation.setDelay(Duration.seconds(2));
			animation.play();
		}

		case LEVEL_STARTING -> {
			maze2D.getFlashingAnimation().setCycleCount(2 * game.numFlashes);
			gameController.stateTimer().setSeconds(1).start();
		}

		case GAME_OVER -> {
			maze2D.getEnergizerAnimation().reset();
			SoundManager.get().stopAll();
		}

		default -> {
			log("PlayScene entered game state %s", e.newGameState);
		}

		}

		// exit GHOST_DYING
		if (e.oldGameState == GameState.GHOST_DYING) {
			game.player.show();
		}
	}

	@Override
	public void doRender() {
		maze2D.render(gc);
		levelCounter2D.render(gc);
		livesCounter2D.render(gc);
		score2D.render(gc);
		highScore2D.render(gc);
		if (gameController.state == GameState.GAME_OVER || gameController.attractMode) {
			gc.setFont(r2D.getArcadeFont());
			gc.setFill(Color.RED);
			gc.fillText("GAME", t(9), t(21));
			gc.fillText("OVER", t(15), t(21));
		} else if (gameController.state == GameState.READY) {
			gc.setFont(r2D.getArcadeFont());
			gc.setFill(Color.YELLOW);
			gc.fillText("READY!", t(11), t(21));
		}
		bonus2D.render(gc);
		player2D.render(gc);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(gc));
	}
}