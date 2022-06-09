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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * @author Armin Reichert
 */
public class GuysInfo {

	private GameModel game;
	private double scaling;
	private final Text[] texts = new Text[6];

	public GuysInfo(Pane parent) {
		for (int i = 0; i < texts.length; ++i) {
			texts[i] = new Text();
			texts[i].setTextAlignment(TextAlignment.CENTER);
			texts[i].setFill(Color.WHITE);
		}
		parent.getChildren().addAll(texts);
	}

	public void init(GameModel game, double scaling) {
		this.game = game;
		this.scaling = scaling;
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

	private String computeGhostInfo(Ghost ghost) {
		String stateText = ghost.state.name();
		if (ghost.state == GhostState.HUNTING_PAC) {
			stateText += game.huntingTimer.chasingPhase() != -1 ? " (Chasing)" : " (Scattering)";
		}
		var animKey = ghost.animations().get().selectedKey();
		var animState = getAnimationState(ghost.animations().get().selectedAnimation(), ghost.wishDir());
		return "%s\n%s\n %s%s".formatted(ghost.name, stateText, animState, animKey);
	}

	private String computePacInfo(Pac pac) {
		if (pac.animations().isPresent()) {
			var animKey = pac.animations().get().selectedKey();
			var animState = getAnimationState(pac.animations().get().selectedAnimation(), pac.moveDir());
			return "%s\n%s%s".formatted(pac.name, animState, animKey);
		} else {
			return "%s\n".formatted(pac.name);
		}
	}

	private String computeBonusInfo(Bonus bonus) {
		var symbolName = bonus.state() == BonusState.INACTIVE ? "INACTIVE"
				: PlayScene2D.bonusName(game.variant, bonus.symbol());
		if (bonus.animations().isPresent()) {
			return "%s\n%s\n%s".formatted(symbolName, game.bonus().state(), bonus.animations().get().selectedKey());
		} else {
			return "%s\n%s".formatted(symbolName, game.bonus().state());
		}
	}

	private void updateTextView(Text textView, String text, Entity entity) {
//			double scaling = fxSubScene.getHeight() / unscaledSize.y;
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		textView.setX((entity.position.x + World.HTS) * scaling - textSize.getWidth() / 2);
		textView.setY(entity.position.y * scaling - textSize.getHeight());
		textView.setVisible(entity.visible);
	}

	private void updateTextView(Text textView, String text, Bonus bonus) {
//			double scaling = fxSubScene.getHeight() / unscaledSize.y;
		textView.setText(text);
		var textSize = textView.getBoundsInLocal();
		textView.setX((bonus.position().x + World.HTS) * scaling - textSize.getWidth() / 2);
		textView.setY(bonus.position().y * scaling - textSize.getHeight());
		textView.setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void update() {
		for (int i = 0; i < texts.length; ++i) {
			if (i < texts.length - 2) {
				var ghost = game.ghosts[i];
				updateTextView(texts[i], computeGhostInfo(ghost), ghost);
			} else if (i == texts.length - 2) {
				updateTextView(texts[i], computePacInfo(game.pac), game.pac);
			} else {
				updateTextView(texts[i], computeBonusInfo(game.bonus()), game.bonus());
			}
		}
	}
}