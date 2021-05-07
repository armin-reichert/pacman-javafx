package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

/**
 * Common interface of perspectives (camera settings) of the play scene.
 * 
 * @author Armin Reichert
 */
public interface PlayScenePerspective extends EventHandler<KeyEvent> {

	@Override
	default void handle(KeyEvent event) {
	}

	void reset();

	void follow(Node target);

	default double approach(double current, double target) {
		return current + (target - current) * 0.02;
	}
}