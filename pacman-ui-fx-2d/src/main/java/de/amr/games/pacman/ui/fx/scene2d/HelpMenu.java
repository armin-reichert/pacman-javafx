/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.oneOf;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class HelpMenu extends VBox {

	private final FadeTransition fadingTransition;

	public HelpMenu() {
		fadingTransition = new FadeTransition(Duration.seconds(0.5), this);
		fadingTransition.setFromValue(1);
		fadingTransition.setToValue(0);
	}

	/**
	 * Makes the help root visible for given duration and then plays the close animation.
	 * 
	 * @param menus          available menu collection
	 * @param gameController the game controller
	 * @param openDuration   duration the menu stays open
	 */
	public void show(HelpMenuFactory menus, GameController gameController, Duration openDuration) {
		var game = gameController.game();
		var gameState = gameController.state();
		Pane menu = null;
		if (gameState == GameState.INTRO) {
			menu = menus.menuIntro(gameController);
		} else if (gameState == GameState.CREDIT) {
			menu = menus.menuCredit(gameController);
		} else if (oneOf(gameState, GameState.READY, GameState.HUNTING, GameState.PACMAN_DYING, GameState.GHOST_DYING)) {
			if (game.level().isPresent()) {
				menu = game.level().get().isDemoLevel() ? menus.menuDemoLevel(gameController)
						: menus.menuPlaying(gameController);
			}
		}
		if (menu == null) {
			getChildren().clear();
		} else {
			getChildren().setAll(menu);
		}
		setOpacity(1);
		if (fadingTransition.getStatus() == Status.RUNNING) {
			fadingTransition.playFromStart();
		}
		fadingTransition.setDelay(openDuration);
		fadingTransition.play();
	}
}