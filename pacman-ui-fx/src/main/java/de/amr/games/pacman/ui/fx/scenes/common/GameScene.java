package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
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

	default void end() {
	}

	void setController(PacManGameController controller);

	default void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
	}

	default OptionalDouble aspectRatio() {
		return OptionalDouble.of(UNSCALED_SCENE_WIDTH / UNSCALED_SCENE_HEIGHT);
	}

	void setAvailableSize(double width, double height);

	SubScene getFXSubScene();

	void useMoveableCamera(boolean use);

	Camera getActiveCamera();
}