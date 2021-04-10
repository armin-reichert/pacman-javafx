package de.amr.games.pacman.ui.fx.scenes.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.Optional;
import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEventListener;
import de.amr.games.pacman.model.common.AbstractGameModel;
import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends PacManGameEventListener {

	public static final int UNSCALED_SCENE_WIDTH = 28 * TS;
	public static final int UNSCALED_SCENE_HEIGHT = 36 * TS;

	void start();

	void update();

	void end();

	PacManGameController getGameController();

	void setGameController(PacManGameController gameController);

	default AbstractGameModel game() {
		return getGameController() != null ? getGameController().game() : null;
	}

	OptionalDouble aspectRatio();

	void stretchTo(double width, double height);

	SubScene getFXSubScene();

	default void selectCamera(CameraType cameraType) {
	}

	default Optional<Camera> selectedCamera() {
		return Optional.empty();
	}
}