package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;

public interface PlayScenePerspective {

	Camera camera();

	void reset();

	void follow(Node target);

	default double approach(double current, double target) {
		return current + (target - current) * 0.02;
	}
}