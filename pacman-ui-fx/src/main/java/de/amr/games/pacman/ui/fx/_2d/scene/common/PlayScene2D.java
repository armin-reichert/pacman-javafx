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
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimation;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.PacAnimation;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MyGhostAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MyPacAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private class InfoPane extends Pane {

		private Text pacInfo = new Text();
		private Text[] ghostInfos = new Text[4];

		public InfoPane() {
			for (int id = 0; id < 4; ++id) {
				ghostInfos[id] = new Text();
				ghostInfos[id].setTextAlignment(TextAlignment.CENTER);
				ghostInfos[id].setFill(Color.WHITE);
				getChildren().add(ghostInfos[id]);
			}
			pacInfo.setFill(Color.WHITE);
			getChildren().add(pacInfo);
			root.getChildren().add(this);
		}

		public void update() {
			for (int id = 0; id < 4; ++id) {
				var ghost2D = ghosts2D[id];
				var ghost = game.ghosts[id];
				String text = "%s\n(%s %s)".formatted(ghost.name, ghost.state, ghost2D.animations.selectedKey());
				ghostInfos[id].setText(text);
				Bounds bounds = ghostInfos[id].getBoundsInLocal();
				ghostInfos[id].setX((ghost.position.x + World.HTS) * scaling() - bounds.getWidth() / 2);
				ghostInfos[id].setY(ghost.position.y * scaling() - 30);
				ghostInfos[id].setVisible(ghost.visible);
			}
			String text = "%s (%s)".formatted(game.pac.name, pac2D.animations.selectedKey());
			pacInfo.setText(text);
			Bounds bounds = pacInfo.getBoundsInLocal();
			pacInfo.setX((game.pac.position.x + World.HTS) * scaling() - bounds.getWidth() / 2);
			pacInfo.setY(game.pac.position.y * scaling() - 20);
			pacInfo.setVisible(game.pac.visible);
		}
	}

	private Maze2D maze2D;
	private LivesCounter2D livesCounter2D;
	private LevelCounter2D levelCounter2D;
	private Pac2D pac2D;
	private Ghost2D[] ghosts2D = new Ghost2D[4];
	private Bonus2D bonus2D;
	private final InfoPane infoPane;

	public PlayScene2D(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		infoPane = new InfoPane();
	}

	@Override
	public void onKeyPressed(KeyCode code) {
		if (code == KeyCode.DIGIT5) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		}
	}

	@Override
	public void init() {
		boolean hasCredit = gameController.credit() > 0;
		SoundManager.get().setStopped(!hasCredit);

		createCommonParts(game);
		score2D.showPoints = hasCredit;
		credit2D.visible = !hasCredit;
		livesCounter2D = new LivesCounter2D(game, t(2), t(34));
		livesCounter2D.visible = hasCredit;
		levelCounter2D = new LevelCounter2D(game, unscaledSize.x - t(4), unscaledSize.y - t(2));
		levelCounter2D.visible = hasCredit;

		maze2D = new Maze2D(game, 0, t(3));

		pac2D = new Pac2D(game.pac, game, new MyPacAnimationSet(r2D));
		for (Ghost ghost : game.ghosts) {
			ghosts2D[ghost.id] = new Ghost2D(ghost, game, new MyGhostAnimationSet(ghost.id, r2D));
		}
		bonus2D = new Bonus2D(game::bonus);
		if (game.variant == GameVariant.MS_PACMAN) {
			bonus2D.setJumpAnimation(Rendering2D_MsPacMan.get().createBonusJumpAnimation());
		}
	}

	@Override
	public void end() {
		log("Scene '%s' ended", getClass().getName());
		SoundManager.get().setStopped(false);
	}

	@Override
	protected void doUpdate() {
		updateAnimations();
		updateSound();
		if (GameUI.debug) {
			infoPane.update();
			infoPane.setVisible(true);
		} else {
			infoPane.setVisible(false);
		}
	}

	private void updateAnimations() {
		long recoveringTicks = sec_to_ticks(2); // TODO not sure about recovering duration
		boolean recoveringStarts = game.pac.powerTimer.remaining() == recoveringTicks;
		boolean recovering = game.pac.powerTimer.remaining() <= recoveringTicks;
		if (recoveringStarts) {
			for (var ghost2D : ghosts2D) {
				ghost2D.animations.startFlashing(game.level.numFlashes, recoveringTicks);
			}
		}
		for (var ghost2D : ghosts2D) {
			ghost2D.updateAnimation(game.pac.hasPower(), recovering);
		}
	}

	private void updateSound() {
		if (gameController.credit() == 0) {
			return;
		}
		switch (gameController.state()) {
		case HUNTING -> {
			if (SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying() && game.pac.starvingTicks > 10) {
				SoundManager.get().stop(GameSound.PACMAN_MUNCH);
			}
			if (game.huntingTimer.tick() == 0) {
				SoundManager.get().stopSirens();
				SoundManager.get().startSiren(game.huntingTimer.phase() / 2);
			}
		}
		default -> {
		}
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		score2D.render(g, r2D);
		highScore2D.render(g, r2D);
		livesCounter2D.render(g, r2D);
		levelCounter2D.render(g, r2D);
		credit2D.render(g, r2D);
		maze2D.render(g, r2D);
		drawGameStateMessage(g);
		bonus2D.render(g, r2D);
		pac2D.render(g, r2D);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(g, r2D));
	}

	private void drawGameStateMessage(GraphicsContext g) {
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
	}

	public void onSwitchFrom3DScene() {
		pac2D.visible = pac2D.pac.visible;
		pac2D.animations.animation(PacAnimation.MUNCHING).ensureRunning();
		for (Ghost2D ghost2D : ghosts2D) {
			ghost2D.visible = ghost2D.ghost.visible;
			ghost2D.animations.ensureAllRunning();
		}
		maze2D.getEnergizerAnimation().restart();
		AudioClip munching = SoundManager.get().getClip(GameSound.PACMAN_MUNCH);
		if (munching.isPlaying() && game.pac.starvingTicks > 10) {
			SoundManager.get().stop(GameSound.PACMAN_MUNCH);
		}
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
		if (!SoundManager.get().isAnySirenPlaying()) {
			SoundManager.get().startSiren(game.huntingTimer.phase() / 2);
		}
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		SoundManager.get().stopSirens();
		if (!SoundManager.get().getClip(GameSound.PACMAN_POWER).isPlaying()) {
			SoundManager.get().loop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		if (!SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying()) {
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
		SoundManager.get().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostStartsReturningHome(GameEvent e) {
		SoundManager.get().playIfOff(GameSound.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			SoundManager.get().stop(GameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		case READY -> {
			SoundManager.get().stopAll();
			maze2D.getEnergizerAnimation().reset();
			pac2D.animations.selectAnimation(PacAnimation.MUNCHING);
			Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animations.restart());
			if (!gameController.isGameRunning()) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}
		case HUNTING -> {
			maze2D.getEnergizerAnimation().restart();
			pac2D.animations.restart();
			Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animations.restart(GhostAnimation.COLOR));
		}
		case PACMAN_DYING -> {
			gameController.state().timer().setDurationIndefinite();
			gameController.state().timer().start();
			SoundManager.get().stopAll();
			pac2D.animations.selectAnimation(PacAnimation.DYING);
			pac2D.animations.selectedAnimation().stop();
			new SequentialTransition( //
					pauseSec(1, () -> game.ghosts().forEach(Ghost::hide)), //
					pauseSec(1, () -> {
						SoundManager.get().play(GameSound.PACMAN_DEATH);
						pac2D.animations.selectedAnimation().run();
					}), //
					pauseSec(2, () -> game.pac.hide()), //
					pauseSec(1, () -> gameController.state().timer().expire()) // exit game state
			).play();
		}
		case GHOST_DYING -> {
			game.pac.hide();
			SoundManager.get().play(GameSound.GHOST_EATEN);
		}
		case LEVEL_COMPLETE -> {
			gameController.state().timer().setDurationIndefinite(); // wait until continueGame() is called
			SoundManager.get().stopAll();
			pac2D.animations.reset();
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
			gameController.state().timer().setDurationSeconds(1);
			gameController.state().timer().start();
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