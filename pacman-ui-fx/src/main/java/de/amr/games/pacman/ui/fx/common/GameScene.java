package de.amr.games.pacman.ui.fx.common;

import java.util.Optional;

/**
 * A game scene that gets drawn into a canvas.
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	public void start();

	public void update();

	public void end();

	default void updateCamera() {
	}

	default Optional<ControllableCamera> getCamera() {
		return Optional.empty();
	}

	default boolean isCameraEnabled() {
		return false;
	}

	default void enableCamera(boolean state) {
	}
}