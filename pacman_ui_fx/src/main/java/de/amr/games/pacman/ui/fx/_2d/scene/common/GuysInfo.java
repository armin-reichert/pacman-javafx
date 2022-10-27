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

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationByDirection;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * @author Armin Reichert
 */
public class GuysInfo {

	private static final String[] PACMAN_BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES", "GALAXIAN",
			"BELL", "KEY" };

	private static final String[] MS_PACMAN_BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR",
			"BANANA" };

	private final GameScene2D hostScene;
	private final List<Pane> panes = new ArrayList<>();
	private final List<Text> texts = new ArrayList<>();

	public GuysInfo(GameScene2D hostScene) {
		this.hostScene = hostScene;
		for (int i = 0; i < 6; ++i) {
			var text = new Text();
			text.setTextAlignment(TextAlignment.CENTER);
			text.setFill(Color.WHITE);
			texts.add(text);
		}
		for (int i = 0; i < 6; ++i) {
			var pane = new VBox(texts.get(i));
			pane.setBackground(Ufx.colorBackground(Color.rgb(200, 200, 255, 0.5)));
			panes.add(pane);
		}
		hostScene.infoLayer.getChildren().addAll(panes);
	}

	private String locationInfo(Creature guy) {
		return "Tile: %s%s%s".formatted(guy.tile(), guy.offset(), guy.isStuck() ? " stuck" : "");
	}

	private String movementInfo(Creature guy) {
		return "Velocity: %s%ndir:%s wish:%s".formatted(guy.getVelocity(), guy.moveDir(), guy.wishDir());
	}

	private String fmtAnimationState(EntityAnimation animation, Direction dir) {
		if (animation instanceof EntityAnimationByDirection dam) {
			return dam.get(dir).isRunning() ? "" : "(Stopped) ";
		} else if (animation instanceof SingleEntityAnimation<?> ssa) {
			return ssa.isRunning() ? "" : "(Stopped) ";
		} else {
			return "";
		}
	}

	private String fmtGhostInfo(Ghost ghost) {
		var game = hostScene.ctx.game();
		String name = ghost.id == Ghost.RED_GHOST && game.cruiseElroyState > 0 ? "Elroy " + game.cruiseElroyState
				: ghost.name;
		var stateText = ghost.getState().name();
		if (ghost.is(GhostState.HUNTING_PAC)) {
			stateText += game.huntingTimer.inChasingPhase() ? " (Chasing)" : " (Scattering)";
		}
		if (game.killedIndex[ghost.id] != -1) {
			stateText += " killed: %d".formatted(game.killedIndex[ghost.id]);
		}
		var selectedAnim = ghost.animation();
		if (selectedAnim.isPresent()) {
			var key = ghost.animationSet().get().selectedKey();
			var animState = fmtAnimationState(selectedAnim.get(), ghost.wishDir());
			return "%s%n%s%n%s%n%s %s%s".formatted(name, locationInfo(ghost), movementInfo(ghost), stateText, animState, key);
		}
		return "%s%n%s%n%s%n%s".formatted(name, locationInfo(ghost), movementInfo(ghost), stateText);
	}

	private String fmtPacInfo(Pac pac) {
		var selectedAnim = pac.animation();
		if (selectedAnim.isPresent()) {
			var key = pac.animationSet().get().selectedKey();
			var animState = fmtAnimationState(selectedAnim.get(), pac.moveDir());
			return "%s%n%s%n%s%n%s%s".formatted(pac.name, locationInfo(pac), movementInfo(pac), animState, key);
		} else {
			return "%s%n%s%n%s".formatted(pac.name, locationInfo(pac), movementInfo(pac));
		}
	}

	private String fmtBonusInfo(Bonus bonus) {
		var game = hostScene.ctx.game();
		var bonusName = switch (game.variant) {
		case MS_PACMAN -> MS_PACMAN_BONUS_NAMES[bonus.index()];
		case PACMAN -> PACMAN_BONUS_NAMES[bonus.index()];
		};
		var symbolText = bonus.state() == BonusState.INACTIVE ? "INACTIVE" : bonusName;
		return "%s%n%s".formatted(symbolText, game.bonus().state());
	}

	private void updateTextView(int i, String text, Entity entity) {
		var textView = texts.get(i);
		var pane = panes.get(i);
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		var scaling = hostScene.getScaling();
		pane.setTranslateX((entity.getPosition().x() + World.HTS) * scaling - textSize.getWidth() / 2);
		pane.setTranslateY(entity.getPosition().y() * scaling - textSize.getHeight());
		pane.setVisible(entity.isVisible());
	}

	private void updateTextView(int i, String text, Bonus bonus) {
		var textView = texts.get(i);
		var pane = panes.get(i);
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		var scaling = hostScene.getScaling();
		pane.setTranslateX((bonus.entity().getPosition().x() + World.HTS) * scaling - textSize.getWidth() / 2);
		pane.setTranslateY(bonus.entity().getPosition().y() * scaling - textSize.getHeight());
		pane.setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void update() {
		var game = hostScene.ctx.game();
		for (int i = 0; i < texts.size(); ++i) {
			if (i < texts.size() - 2) {
				var ghost = game.theGhosts[i];
				updateTextView(i, fmtGhostInfo(ghost), ghost);
			} else if (i == texts.size() - 2) {
				updateTextView(i, fmtPacInfo(game.pac), game.pac);
			} else {
				updateTextView(i, fmtBonusInfo(game.bonus()), game.bonus());
			}
		}
	}
}