package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Node;

/**
 * Common interface of perspectives (camera settings) of the play scene.
 * 
 * @author Armin Reichert
 */
public interface PlayScenePerspective {

	void reset();

	void follow(Node target);

	default double approach(double current, double target) {
		return current + (target - current) * 0.02;
	}
}