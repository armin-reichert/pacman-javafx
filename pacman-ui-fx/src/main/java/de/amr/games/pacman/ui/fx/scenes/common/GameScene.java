package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEventListener;
import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends PacManGameEventListener {

	void start();

	void update();

	void end();

	PacManGameController getController();

	void setGameController(PacManGameController controller);

	OptionalDouble aspectRatio();

	void setAvailableSize(double width, double height);

	SubScene getFXSubScene();

	void useMoveableCamera(boolean use);

	Camera getActiveCamera();

	void stopAllSounds();
}