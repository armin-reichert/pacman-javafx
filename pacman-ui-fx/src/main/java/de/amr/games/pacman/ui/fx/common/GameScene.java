package de.amr.games.pacman.ui.fx.common;

import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	public static final double UNSCALED_SCENE_WIDTH = 28 * 8;

	public static final double UNSCALED_SCENE_HEIGHT = 36 * 8;

	public static final double ASPECT_RATIO = UNSCALED_SCENE_WIDTH / UNSCALED_SCENE_HEIGHT;

	void start();

	void update();

	void end();

	void setAvailableSize(double width, double height);

	SubScene getFXSubScene();

	void useMoveableCamera(boolean use);

	Camera getActiveCamera();
}