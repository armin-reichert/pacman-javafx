/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class GameSceneMenu extends BorderPane {

	private final FadeTransition fading;

	public GameSceneMenu() {
		fading = new FadeTransition(Duration.seconds(0.5), this);
		fading.setFromValue(1);
		fading.setToValue(0);
	}

	public void setContent(Node content) {
		setCenter(content);
	}

	/**
	 * Makes the menu visible for given duration and then plays the close animation.
	 * 
	 * @param fadingDelay duration before menu starts fading out
	 */
	public void show(Duration fadingDelay) {
		if (fading.getStatus() == Status.RUNNING) {
			return;
		}
		setOpacity(1);
		fading.setDelay(fadingDelay);
		fading.play();
	}
}