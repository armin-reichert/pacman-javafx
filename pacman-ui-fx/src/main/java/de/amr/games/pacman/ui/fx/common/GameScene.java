package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface of all game scenes.
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

	void resize(double width, double height);

	SubScene getSubScene();

	void useMoveableCamera(boolean use);

	Camera getActiveCamera();
}