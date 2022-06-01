/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx.util.U.pauseSec;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D.AnimationKey;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
	public void handleKeyPressed(KeyEvent e) {
		if (GameUI.pressed(e, KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		}
	}

	@Override
	public void init() {
		boolean hasCredit = gameController.credit() > 0;
		createCommonParts(game);
		score2D.showPoints = hasCredit;
		credit2D.visible = !hasCredit;
		livesCounter2D = new LivesCounter2D(game, t(2), t(34));
		livesCounter2D.visible = hasCredit;
		levelCounter2D = new LevelCounter2D(game, unscaledSize.x - t(4), unscaledSize.y - t(2));
		levelCounter2D.visible = hasCredit;
		maze2D = new Maze2D(game, 0, t(3));
		player2D = new Player2D(game.player, game).createAnimations(r2D);
		for (Ghost ghost : game.ghosts) {
			ghosts2D[ghost.id] = new Ghost2D(ghost, game).createAnimations(r2D);
		}
		bonus2D = new Bonus2D(game::bonus);
		if (game.variant == GameVariant.MS_PACMAN) {
			bonus2D.setJumpAnimation(Rendering2D_MsPacMan.get().createBonusJumpAnimation());
		}
	}

	@Override
	public void end() {
		log("Scene '%s' ended", getClass().getName());
	}

	@Override
	protected void doUpdate() {
		updateAnimations();
		updateSound();
	}

	@Override
	public void doRender(GraphicsContext g) {
		score2D.render(g, r2D);
		highScore2D.render(g, r2D);
		livesCounter2D.render(g, r2D);
		credit2D.render(g, r2D);
		maze2D.render(g, r2D);
		if (gameController.state() == GameState.GAME_OVER || gameController.credit() == 0) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (gameController.state() == GameState.READY) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.YELLOW);
			g.fillText("READY!", t(11), t(21));
		}
		bonus2D.render(g, r2D);
		player2D.render(g, r2D);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(g, r2D));
		levelCounter2D.render(g, r2D);
	}

	private void updateAnimations() {
		boolean frightened = game.player.hasPower();
		boolean flashing = game.player.powerTimer.remaining() <= sec_to_ticks(2);
		for (var ghost2D : ghosts2D) {
			switch (ghost2D.ghost.state) {
			case DEAD -> ghost2D.selectAnimation(ghost2D.ghost.bounty == 0 ? AnimationKey.EYES : AnimationKey.NUMBER);
			case ENTERING_HOUSE -> ghost2D.selectAnimation(AnimationKey.EYES);
			case FRIGHTENED -> ghost2D.selectAnimation(flashing ? AnimationKey.FLASHING : AnimationKey.FRIGHTENED);
			case HUNTING_PAC, LEAVING_HOUSE -> ghost2D.selectAnimation(AnimationKey.KICKING);
			case LOCKED -> ghost2D.selectAnimation(
					flashing ? AnimationKey.FLASHING : frightened ? AnimationKey.FRIGHTENED : AnimationKey.KICKING);
			}
		}
	}

	private void updateSound() {
		if (gameController.credit() == 0) {
			return;
		}
		switch (gameController.state()) {
		case HUNTING -> {
			if (SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying() && game.player.starvingTicks > 10) {
				SoundManager.get().stop(GameSound.PACMAN_MUNCH);
			}
			if (game.huntingTimer.scatteringPhase() >= 0 && game.huntingTimer.tick() == 0) {
				SoundManager.get().stopSirens();
				SoundManager.get().startSiren(game.huntingTimer.scatteringPhase());
			}
			if (game.huntingTimer.chasingPhase() >= 0 && !SoundManager.get().isAnySirenPlaying()) {
				SoundManager.get().startSiren(game.huntingTimer.chasingPhase());
			}
		}
		default -> {
		}
		}
	}

	public void onSwitchFrom3DScene() {
		player2D.visible = game.player.visible;
		for (Ghost2D ghost2D : ghosts2D) {
			ghost2D.visible = ghost2D.ghost.visible;
		}
		if (!player2D.animMunching.get(game.player.moveDir()).isRunning()) {
			player2D.animMunching.values().forEach(SpriteAnimation::restart);
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
	public void onPlayerLosesPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		Stream.of(ghosts2D).filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
			ghost2D.animFlashing.reset();
			ghost2D.animFrightened.restart();
		});
		SoundManager.get().stopSirens();
		if (gameController.credit() > 0 && !SoundManager.get().getClip(GameSound.PACMAN_POWER).isPlaying()) {
			SoundManager.get().loop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		if (gameController.credit() > 0 && !SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying()) {
			SoundManager.get().loop(GameSound.PACMAN_MUNCH, Animation.INDEFINITE);
		}
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		bonus2D.startJumping();
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		bonus2D.stopJumping();
		if (gameController.credit() > 0) {
			SoundManager.get().play(GameSound.BONUS_EATEN);
		}
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostStartsReturningHome(GameEvent e) {
		if (gameController.credit() > 0) {
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
			player2D.resetAnimations();
			Stream.of(ghosts2D).forEach(Ghost2D::resetAnimations);
			if (gameController.credit() > 0 && !gameController.isGameRunning()) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}

		case HUNTING -> {
			maze2D.getEnergizerAnimation().restart();
			player2D.animMunching.values().forEach(SpriteAnimation::restart);
			Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animKicking.values().forEach(SpriteAnimation::restart));
		}

		case PACMAN_DYING -> {
			// wait until game is continued
			gameController.state().timer().setDurationIndefinite().start();
			SoundManager.get().stopAll();
			new SequentialTransition( //
					pauseSec(1, () -> game.ghosts().forEach(Ghost::hide)), //
					pauseSec(1, () -> player2D.startDyingAnimation(gameController.credit() > 0)), //
					pauseSec(2, () -> game.player.hide()), //
					pauseSec(1, () -> gameController.state().timer().expire()) //
			).play();
		}

		case GHOST_DYING -> {
			game.player.hide();
			if (gameController.credit() > 0) {
				SoundManager.get().play(GameSound.GHOST_EATEN);
			}
		}

		case LEVEL_COMPLETE -> {
			gameController.state().timer().setDurationIndefinite(); // wait until continueGame() is called
			SoundManager.get().stopAll();
			player2D.resetAnimations();
			// Energizers can still exist if "next level" cheat has been used
			maze2D.getEnergizerAnimation().reset();
			Animation animation = new SequentialTransition( //
					maze2D.getFlashingAnimation(), //
					pauseSec(1, () -> gameController.state().timer().expire()) //
			);
			animation.setDelay(Duration.seconds(2));
			animation.play();
		}

		case LEVEL_STARTING -> {
			maze2D.getFlashingAnimation().setCycleCount(2 * game.level.numFlashes);
			gameController.state().timer().setDurationSeconds(1).start();
		}

		case GAME_OVER -> {
			maze2D.getEnergizerAnimation().reset();
			SoundManager.get().stopAll();
		}

		default -> {
			log("PlayScene entered game state %s", e.newGameState);
		}
		}
	}
}