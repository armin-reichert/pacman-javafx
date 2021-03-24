package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	void start();

	void update();

	void end();

	PacManGameController getController();

	void setController(PacManGameController controller);

	void onGameStateChange(PacManGameState oldState, PacManGameState newState);

	OptionalDouble aspectRatio();

	void setAvailableSize(double width, double height);

	SubScene getFXSubScene();

	void useMoveableCamera(boolean use);

	Camera getActiveCamera();

	void stopAllSounds();
}