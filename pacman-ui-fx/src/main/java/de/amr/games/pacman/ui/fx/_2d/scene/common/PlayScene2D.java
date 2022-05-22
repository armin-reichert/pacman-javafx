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
import static de.amr.games.pacman.ui.fx.util.U.pauseSec;

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
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

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
		createScores();
		score2D.showPoints = !gc.attractMode;

		livesCounter2D = new LivesCounter2D(game, r2D);
		livesCounter2D.x = t(2);
		livesCounter2D.y = t(34);
		livesCounter2D.visible = !gc.attractMode;

		levelCounter2D = new LevelCounter2D(game, r2D);
		levelCounter2D.rightPosition = unscaledSize.minus(t(4), t(2));
		levelCounter2D.visible = !gc.attractMode;

		maze2D = new Maze2D(game, r2D);
		maze2D.x = 0;
		maze2D.y = t(3);

		player2D = new Player2D(game.player, game, r2D);

		for (Ghost ghost : game.ghosts) {
			ghosts2D[ghost.id] = new Ghost2D(ghost, game, r2D);
		}

		boolean bonusMoving = gc.gameVariant == GameVariant.MS_PACMAN;
		bonus2D = new Bonus2D(game.bonus, r2D, bonusMoving);

		game.player.powerTimer.addEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void end() {
		game.player.powerTimer.removeEventListener(this::handleGhostsFlashing);
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
		if (SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying() && game.player.starvingTicks > 10) {
			SoundManager.get().stop(GameSound.PACMAN_MUNCH);
		}
		if (!gc.attractMode && gc.state == GameState.HUNTING && !SoundManager.get().isAnySirenPlaying()
				&& !game.player.powerTimer.isRunning()) {
			int scatterPhase = game.huntingPhase / 2;
			SoundManager.get().startSiren(scatterPhase);
		}
	}

	public void onSwitchFrom3DScene() {
		player2D.visible = game.player.visible;
		for (Ghost2D ghost2D : ghosts2D) {
			ghost2D.visible = ghost2D.ghost.visible;
		}
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
		AudioClip munching = SoundManager.get().getClip(GameSound.PACMAN_MUNCH);
		if (munching.isPlaying() && game.player.starvingTicks > 10) {
			SoundManager.get().stop(GameSound.PACMAN_MUNCH);
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		SoundManager.get().stopSirens();
		if (!gc.attractMode) {
			SoundManager.get().startSiren(e.scatterPhase);
		}
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		Stream.of(ghosts2D).filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
			ghost2D.animFlashing.reset();
			ghost2D.animFrightened.restart();
		});
		SoundManager.get().stopSirens();
		if (!gc.attractMode && !SoundManager.get().getClip(GameSound.PACMAN_POWER).isPlaying()) {
			SoundManager.get().loop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
		}
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		if (!gc.attractMode && !SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying()) {
			SoundManager.get().loop(GameSound.PACMAN_MUNCH, Animation.INDEFINITE);
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus2D.startAnimation();
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus2D.stopAnimation();
		if (!gc.attractMode) {
			SoundManager.get().play(GameSound.BONUS_EATEN);
		}
	}

	@Override
	public void onExtraLife(GameEvent e) {
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		if (!gc.attractMode) {
			SoundManager.get().playIfOff(GameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			SoundManager.get().stop(GameSound.GHOST_RETURNING);
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
			if (!gc.attractMode && !gc.gameRunning) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}

		case HUNTING -> {
			maze2D.getEnergizerAnimation().restart();
			player2D.animMunching.values().forEach(TimedSeq::restart);
			Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animKicking.values().forEach(TimedSeq::restart));
		}

		case PACMAN_DYING -> {
			// wait until game is continued
			gc.state.timer().setIndefinite().start();

			SoundManager.get().stopAll();

			new SequentialTransition( //
					pauseSec(1, () -> game.ghosts().forEach(Ghost::hide)), //
					pauseSec(1, () -> {
						if (!gc.attractMode) {
							SoundManager.get().play(GameSound.PACMAN_DEATH);
						}
						player2D.playDyingAnimation();
					}), //
					pauseSec(2, () -> game.player.hide()), //
					pauseSec(1, () -> gc.state.timer().expire()) //
			).play();
		}

		case GHOST_DYING -> {
			game.player.hide();
			if (!gc.attractMode) {
				SoundManager.get().play(GameSound.GHOST_EATEN);
			}
		}

		case LEVEL_COMPLETE -> {
			gc.state.timer().setIndefinite(); // wait until continueGame() is called
			SoundManager.get().stopAll();
			player2D.reset();
			// Energizers can still exist if "next level" cheat has been used
			maze2D.getEnergizerAnimation().reset();
			Animation animation = new SequentialTransition( //
					maze2D.getFlashingAnimation(), //
					pauseSec(1, () -> gc.state.timer().expire()) //
			);
			animation.setDelay(Duration.seconds(2));
			animation.play();
		}

		case LEVEL_STARTING -> {
			maze2D.getFlashingAnimation().setCycleCount(2 * game.numFlashes);
			gc.state.timer().setSeconds(1).start();
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
	public void doRender(GraphicsContext g) {
		maze2D.render(g);
		levelCounter2D.render(g);
		livesCounter2D.render(g);
		score2D.render(g);
		highScore2D.render(g);
		if (gc.state == GameState.GAME_OVER || gc.attractMode) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (gc.state == GameState.READY) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.YELLOW);
			g.fillText("READY!", t(11), t(21));
		}
		bonus2D.render(g);
		player2D.render(g);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(g));
	}
}