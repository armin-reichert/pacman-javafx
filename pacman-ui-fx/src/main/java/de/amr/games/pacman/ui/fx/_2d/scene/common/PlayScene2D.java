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
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx.util.U.pauseSec;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusAnimationKey;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimationKey;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.actors.PacAnimationKey;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.World2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	static String bonusName(GameVariant gameVariant, int symbol) {
		return switch (gameVariant) {
		case MS_PACMAN -> MsPacManGame.BONUS_NAMES[symbol];
		case PACMAN -> PacManGame.BONUS_NAMES[symbol];
		};
	}

	private class GuysInfo {

		private final Text[] texts = new Text[6];

		public GuysInfo() {
			for (int i = 0; i < texts.length; ++i) {
				texts[i] = new Text();
				texts[i].setTextAlignment(TextAlignment.CENTER);
				texts[i].setFill(Color.WHITE);
			}
			infoPane.getChildren().addAll(texts);
		}

		private String getAnimationState(GenericAnimation animation, Direction dir) {
			if (animation instanceof GenericAnimationMap) {
				@SuppressWarnings("unchecked")
				var gam = (GenericAnimationMap<Direction, ?>) animation;
				return gam.get(dir).isRunning() ? "" : "(Stopped) ";
			} else {
				var ga = (SingleGenericAnimation<?>) animation;
				return ga.isRunning() ? "" : "(Stopped) ";
			}
		}

		private String computeGhostInfo(Ghost2D ghost2D) {
			var ghost = ghost2D.ghost;
			String stateText = ghost.state.name();
			if (ghost.state == GhostState.HUNTING_PAC) {
				stateText += game.huntingTimer.chasingPhase() != -1 ? " (Chasing)" : " (Scattering)";
			}
			var animKey = ghost.animations.selectedKey();
			var animState = getAnimationState(ghost.animations.selectedAnimation(), ghost.wishDir());
			return "%s\n%s\n %s%s".formatted(ghost.name, stateText, animState, animKey);
		}

		private String computePacInfo(Pac pac) {
			var animKey = pac.animations.selectedKey();
			var animState = getAnimationState(pac.animations.selectedAnimation(), pac.moveDir());
			return "%s\n%s%s".formatted(pac.name, animState, animKey);
		}

		private String computeBonusInfo(Bonus2D bonus2D) {
			var bonus = game.bonus();
			var symbolName = bonus.state() == BonusState.INACTIVE ? "INACTIVE" : bonusName(game.variant, bonus.symbol());
			return "%s\n%s\n%s".formatted(symbolName, game.bonus().state(), bonus2D.animations.selectedKey());
		}

		private void updateTextView(Text textView, String text, Entity entity) {
			double scaling = fxSubScene.getHeight() / unscaledSize.y;
			textView.setText(text);
			var textSize = textView.getBoundsInLocal();
			textView.setX((entity.position.x + World.HTS) * scaling - textSize.getWidth() / 2);
			textView.setY(entity.position.y * scaling - textSize.getHeight());
			textView.setVisible(entity.visible);
		}

		private void updateTextView(Text textView, String text, Bonus bonus) {
			double scaling = fxSubScene.getHeight() / unscaledSize.y;
			textView.setText(text);
			var textSize = textView.getBoundsInLocal();
			textView.setX((bonus.position().x + World.HTS) * scaling - textSize.getWidth() / 2);
			textView.setY(bonus.position().y * scaling - textSize.getHeight());
			textView.setVisible(bonus.state() != BonusState.INACTIVE);
		}

		public void update() {
			for (int i = 0; i < texts.length; ++i) {
				if (i < texts.length - 2) {
					updateTextView(texts[i], computeGhostInfo(ghosts2D[i]), ghosts2D[i].ghost);
				} else if (i == texts.length - 2) {
					updateTextView(texts[i], computePacInfo(game.pac), pac2D.pac);
				} else {
					updateTextView(texts[i], computeBonusInfo(bonus2D), game.bonus());
				}
			}
		}
	}

	private World2D world2D;
	private LivesCounter2D livesCounter2D;
	private LevelCounter2D levelCounter2D;
	private Pac2D pac2D;
	private Ghost2D[] ghosts2D = new Ghost2D[4];
	private Bonus2D bonus2D;

	private final GuysInfo guysInfo = new GuysInfo();

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && gameController.credit() == 0) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.L)) {
			Actions.addLives(3);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void init() {
		createScoresAndCredit();
		boolean hasCredit = gameController.credit() > 0;

		gameScore2D.showContent = hasCredit;
		credit2D.visible = !hasCredit;

		livesCounter2D = new LivesCounter2D(game);
		livesCounter2D.visible = hasCredit;

		levelCounter2D = new LevelCounter2D(game.levelCounter, r2D);
		levelCounter2D.visible = hasCredit;

		world2D = new World2D(game, r2D);
		pac2D = new Pac2D(game.pac, r2D);
		for (var ghost : game.ghosts) {
			ghosts2D[ghost.id] = new Ghost2D(ghost, r2D);
		}
		bonus2D = new Bonus2D(game.bonus(), r2D);
		bonus2D.stopJumping();

		SoundManager.get().setStopped(!hasCredit);
	}

	@Override
	public void end() {
		log("Scene '%s' ended", getClass().getName());
		SoundManager.get().setStopped(false);
	}

	@Override
	protected void doUpdate() {
		updateGhostAnimations();
		updateSound();
		if (Env.$debugUI.get()) {
			guysInfo.update();
		}
	}

	private void updateGhostAnimations() {
		long powerTicksRemaining = game.pac.powerTimer.remaining();
		boolean startFlashing = powerTicksRemaining == Ghost2D.FLASHING_TIME;
		boolean endFlashing = powerTicksRemaining == 1; // TODO check why == 0 does not work
		for (var ghost2D : ghosts2D) {
			ghost2D.updateAnimations(startFlashing, endFlashing, game.level.numFlashes);
		}
	}

	private void updateSound() {
		switch (gameController.state()) {
		case HUNTING -> {
			if (game.pac.starvingTicks == 10) {
				SoundManager.get().stop(GameSound.PACMAN_MUNCH);
			}
			if (game.huntingTimer.tick() == 0) {
				SoundManager.get().ensureSirenStarted(game.huntingTimer.phase() / 2);
			}
		}
		default -> {
		}
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		gameScore2D.render(g, r2D);
		highScore2D.render(g, r2D);
		livesCounter2D.render(g, r2D);
		levelCounter2D.render(g, r2D);
		credit2D.render(g, r2D);
		world2D.render(g, r2D);
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
		game.pac.animations.animation(PacAnimationKey.ANIM_MUNCHING).restart();
		for (var ghost : game.ghosts) {
			ghost.animations.restart();
		}
		world2D.letEnergizersBlink(true);
		if (game.pac.starvingTicks > 10) {
			SoundManager.get().stop(GameSound.PACMAN_MUNCH);
		}
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
		SoundManager.get().ensureSirenStarted(game.huntingTimer.phase() / 2);
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		SoundManager.get().stopSirens();
		SoundManager.get().ensureLoop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
		for (var ghost : game.ghosts) {
			ghost.animations.select(GhostAnimationKey.ANIM_BLUE);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		SoundManager.get().ensureLoop(GameSound.PACMAN_MUNCH, Animation.INDEFINITE);
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		bonus2D.animations.select(BonusAnimationKey.ANIM_SYMBOL);
		if (game.variant == GameVariant.MS_PACMAN) {
			bonus2D.startJumping();
		}
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		bonus2D.stopJumping();
		bonus2D.animations.select(BonusAnimationKey.ANIM_VALUE);
		SoundManager.get().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		bonus2D.animations.select(BonusAnimationKey.ANIM_NONE);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostStartsReturningHome(GameEvent e) {
		SoundManager.get().ensurePlaying(GameSound.GHOST_RETURNING);
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
			world2D.showEnergizersOn();
			game.pac.animations.select(PacAnimationKey.ANIM_MUNCHING);
			game.pac.animations.selectedAnimation().reset();
			game.ghosts().forEach(ghost -> ghost.animations.restart());
			if (!gameController.isGameRunning()) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}
		case HUNTING -> {
			world2D.letEnergizersBlink(true);
			game.pac.animations.restart();
			game.ghosts().forEach(ghost -> ghost.animations.restart(GhostAnimationKey.ANIM_COLOR));
		}
		case PACMAN_DYING -> {
			gameController.state().timer().setIndefinite();
			gameController.state().timer().start();
			SoundManager.get().stopAll();
			game.pac.animations.select(PacAnimationKey.ANIM_DYING);
			game.pac.animations.selectedAnimation().stop();
			bonus2D.animations.select(BonusAnimationKey.ANIM_NONE);
			new SequentialTransition( //
					pauseSec(1, () -> game.ghosts().forEach(Ghost::hide)), //
					pauseSec(1, () -> {
						SoundManager.get().play(GameSound.PACMAN_DEATH);
						game.pac.animations.selectedAnimation().run();
					}), //
					pauseSec(2, () -> game.pac.hide()), //
					pauseSec(1, () -> gameController.state().timer().expire()) // exit game state
			).play();
		}
		case GHOST_DYING -> {
			SoundManager.get().play(GameSound.GHOST_EATEN);
			for (var ghost : game.ghosts) {
				ghost.animations.animation(GhostAnimationKey.ANIM_FLASHING).stop();
			}
		}
		case LEVEL_COMPLETE -> {
			gameController.state().timer().setIndefinite();
			SoundManager.get().stopAll();
			game.pac.animations.reset();
			// Energizers can still exist if "next level" cheat has been used
			world2D.showEnergizersOn();
			new SequentialTransition( //
					pauseSec(1, () -> world2D.startFlashing(game.level.numFlashes)), //
					pauseSec(2, () -> {
						gameController.state().timer().expire();
					}) //
			).play();
		}
		case LEVEL_STARTING -> {
			gameController.state().timer().setSeconds(1);
			gameController.state().timer().start();
		}
		case GAME_OVER -> {
			world2D.showEnergizersOn(); // TODO check with MAME
			SoundManager.get().stopAll();
		}
		default -> {
			log("PlayScene entered game state %s", e.newGameState);
		}
		}

		// exit state xyz:
		if (e.oldGameState == GameState.GHOST_DYING) {
			for (var ghost : game.ghosts) {
				ghost.animations.animation(GhostAnimationKey.ANIM_FLASHING).run();
			}
		}
	}
}