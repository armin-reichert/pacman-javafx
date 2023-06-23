/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class HelpMenu extends BorderPane {

	private final FadeTransition fadingTransition;

	public HelpMenu() {
		fadingTransition = new FadeTransition(Duration.seconds(0.5), this);
		fadingTransition.setFromValue(1);
		fadingTransition.setToValue(0);
	}

	/**
	 * Makes the menu visible for given duration and then plays the close animation.
	 * 
	 * @param menu        menu pane
	 * @param fadingDelay duration before menu starts fading out
	 */
	public void show(Pane menu, Duration fadingDelay) {
		setCenter(menu);
		setOpacity(1);
		if (fadingTransition.getStatus() == Status.RUNNING) {
			fadingTransition.playFromStart();
		}
		fadingTransition.setDelay(fadingDelay);
		fadingTransition.play();
	}
}