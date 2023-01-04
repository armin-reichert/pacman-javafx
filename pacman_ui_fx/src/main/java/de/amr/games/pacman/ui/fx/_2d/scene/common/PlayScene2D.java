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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.ui.fx._2d.rendering.RendererCommon.mazeNumber;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationSet;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private final ActorsInfo actorsInfo = new ActorsInfo();

	public PlayScene2D() {
		actorsInfo.enabledPy.bind(Env.showDebugInfoPy);
	}

	@Override
	public void update() {
		actorsInfo.update();
		setCreditVisible(!ctx.hasCredit() || ctx.state() == GameState.GAME_OVER);
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			if (!ctx.hasCredit()) { // credit can only be added in attract mode
				Actions.addCredit();
			}
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.L)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void draw() {
		int mazeX = 0;
		int mazeY = 3 * TS;
		var game = ctx.game();
		var r = ctx.r2D();
		ctx.level().ifPresent(level -> {
			boolean energizersHidden = !level.energizerPulse().frame();
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				var flashingAnimation = arcadeWorld.levelCompleteAnimation();
				if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
					boolean flash = (boolean) flashingAnimation.get().frame();
					r.drawEmptyMaze(g, mazeX, mazeY, mazeNumber(game), flash);
				} else {
					r.drawFilledMaze(g, mazeX, mazeY, mazeNumber(game), level.world(), energizersHidden);
				}
			} else {
				r.drawFilledMaze(g, mazeX, mazeY, mazeNumber(game), level.world(), false);
			}
			r.drawBonus(g, level.bonus());
			r.drawGameStateMessage(g, ctx.hasCredit() ? ctx.state() : GameState.GAME_OVER);
			r.drawPac(g, level.pac());
			level.ghosts().forEach(ghost -> r.drawGhost(g, ghost));
			if (!isCreditVisible()) {
				int lives = game.isOneLessLifeDisplayed() ? game.lives() - 1 : game.lives();
				r.drawLivesCounter(g, lives);
			}
			r.drawLevelCounter(g, game.levelCounter());
		});
	}

	@Override
	protected void drawDebugInfo(GameLevel level) {
		super.drawDebugInfo(level);
		if (level.world() instanceof ArcadeWorld arcadeWorld) {
			g.setFill(Color.RED);
			arcadeWorld.upwardBlockedTiles().forEach(tile -> g.fillRect(tile.x() * TS, tile.y() * TS, TS, 1));
		}
	}

	@Override
	public void onSwitchFrom3D() {
		ctx.level().ifPresent(level -> {
			level.pac().animationSet().ifPresent(EntityAnimationSet::ensureRunning);
			level.ghosts().map(Ghost::animationSet).forEach(anim -> anim.ifPresent(EntityAnimationSet::ensureRunning));
		});
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		ctx.sounds().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}

	/** Nested class for displaying info about the actors in this play scene. */
	public class ActorsInfo {

		private static final String[] PACMAN_BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES",
				"GALAXIAN", "BELL", "KEY" };

		private static final String[] MS_PACMAN_BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE",
				"PEAR", "BANANA" };

		// 0..3: ghost info, 4: Pac info, 5: bonus info
		private static final int NUM_INFOS = 6;
		private static final int PAC_INDEX = 4;
		private static final int BONUS_INDEX = 5;

		private final List<Pane> panes = new ArrayList<>();
		private final List<Text> texts = new ArrayList<>();

		public final BooleanProperty enabledPy = new SimpleBooleanProperty();

		public ActorsInfo() {
			for (int i = 0; i < NUM_INFOS; ++i) {
				var text = new Text();
				text.setTextAlignment(TextAlignment.CENTER);
				text.setFill(Color.WHITE);
				texts.add(text);
				var pane = new VBox(text);
				pane.setBackground(Ufx.colorBackground(Color.rgb(200, 200, 255, 0.5)));
				panes.add(pane);
			}
			panes.forEach(PlayScene2D.this::addToOverlayPane);
		}

		public void update() {
			if (!enabledPy.get()) {
				return;
			}
			ctx.level().ifPresent(level -> {
				for (int i = 0; i < 4; ++i) {
					var ghost = level.ghost(i);
					updateInfo(panes.get(i), texts.get(i), ghostInfo(level, ghost), ghost);
				}
				updateInfo(panes.get(PAC_INDEX), texts.get(PAC_INDEX), pacInfo(level.pac()), level.pac());
				var bonus = level.bonus();
				updateInfo(panes.get(BONUS_INDEX), texts.get(BONUS_INDEX), bonusInfo(bonus), bonus.entity());
				panes.get(BONUS_INDEX).setVisible(bonus.state() != BonusState.INACTIVE);
			});
		}

		private void updateInfo(Pane pane, Text text, String info, Entity entity) {
			text.setText(info);
			var textSize = text.getBoundsInLocal();
			pane.setTranslateX((entity.position().x() + World.HTS) * scaling() - textSize.getWidth() / 2);
			pane.setTranslateY(entity.position().y() * scaling() - textSize.getHeight());
			pane.setVisible(entity.isVisible());
		}

		private String locationInfo(Creature guy) {
			return "Tile: %s%s%s".formatted(guy.tile(), guy.offset(), guy.isStuck() ? " stuck" : "");
		}

		private String movementInfo(Creature guy) {
			return "Velocity: %s%ndir:%s wish:%s".formatted(guy.velocity(), guy.moveDir(), guy.wishDir());
		}

		private String animationStateInfo(EntityAnimation animation, Direction dir) {
			if (animation instanceof EntityAnimationByDirection anim) {
				return anim.get(dir).isRunning() ? "" : "(Stopped @%d) ".formatted(animation.frameIndex());
			}
			if (animation instanceof SingleEntityAnimation<?> anim) {
				return anim.isRunning() ? "" : "(Stopped @%d) ".formatted(animation.frameIndex());
			}
			return "";
		}

		private String ghostInfo(GameLevel level, Ghost ghost) {
			String name = ghost.id() == Ghost.ID_RED_GHOST && level.cruiseElroyState() > 0
					? "Elroy " + level.cruiseElroyState()
					: ghost.name();
			var stateText = ghost.state().name();
			if (ghost.is(GhostState.HUNTING_PAC)) {
				stateText += level.chasingPhase().isPresent() ? " (Chasing)" : " (Scattering)";
			}
			if (ghost.killedIndex() != -1) {
				stateText += " killed: #%d".formatted(ghost.killedIndex());
			}
			var selectedAnim = ghost.animation();
			if (selectedAnim.isPresent()) {
				var ghostAnims = ghost.animationSet();
				if (ghostAnims.isPresent()) {
					var key = ghostAnims.get().selectedKey();
					var animState = animationStateInfo(selectedAnim.get(), ghost.wishDir());
					return "%s%n%s%n%s%n%s %s%s".formatted(name, locationInfo(ghost), movementInfo(ghost), stateText, animState,
							key);
				}
			}
			return "%s%n%s%n%s%n%s".formatted(name, locationInfo(ghost), movementInfo(ghost), stateText);
		}

		private String pacInfo(Pac pac) {
			var pacAnims = pac.animationSet();
			if (pacAnims.isPresent()) {
				var selectedAnim = pacAnims.get().selectedAnimation();
				if (selectedAnim.isPresent()) {
					var animState = animationStateInfo(selectedAnim.get(), pac.moveDir());
					return "%s%n%s%n%s%n%s%s".formatted(pac.name(), locationInfo(pac), movementInfo(pac), animState,
							pacAnims.get().selectedKey());
				}
			}
			return "%s%n%s%n%s".formatted(pac.name(), locationInfo(pac), movementInfo(pac));
		}

		private String bonusInfo(Bonus bonus) {
			var bonusName = switch (ctx.gameVariant()) {
			case MS_PACMAN -> MS_PACMAN_BONUS_NAMES[bonus.symbol()];
			case PACMAN -> PACMAN_BONUS_NAMES[bonus.symbol()];
			};
			var text = bonus.state() == BonusState.INACTIVE ? "INACTIVE" : bonusName;
			return "%s%n%s".formatted(text, bonus.state());
		}
	}
}