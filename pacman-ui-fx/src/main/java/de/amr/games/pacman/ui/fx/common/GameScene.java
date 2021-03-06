package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

import javafx.scene.Camera;

/**
 * A game scene that gets drawn into a canvas.
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	public static final int WIDTH_UNSCALED = 28 * TS;
	public static final int HEIGHT_UNSCALED = 36 * TS;
	public static final double ASPECT_RATIO = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;

	void start();

	void update();

	void end();

	Camera getCamera();

	default void updateCamera() {
	}
}