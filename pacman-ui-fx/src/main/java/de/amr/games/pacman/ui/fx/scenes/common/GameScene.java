package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D). Wrapper for a JavaFX subscene of the main
 * scene.
 * 
 * @author Armin Reichert
 */
public interface GameScene extends DefaultPacManGameEventHandler {

	SubScene getSubSceneFX();

	void init();

	void update();

	void end();

	PacManGameController getGameController();

	void setGameController(PacManGameController gameController);

	default GameModel game() {
		return getGameController() != null ? getGameController().game() : null;
	}

	OptionalDouble aspectRatio();

	void resize(double width, double height);

	default void keepSizeOf(Scene parentScene) {
		if (aspectRatio().isPresent()) {
			double aspectRatio = aspectRatio().getAsDouble();
			parentScene.widthProperty().addListener((foo, bar, targetWidth) -> {
				double newHeight = Math.min(targetWidth.doubleValue() / aspectRatio, parentScene.getHeight());
				double newWidth = newHeight * aspectRatio;
				resize(newWidth, newHeight);
			});
			parentScene.heightProperty().addListener((foo, bar, targetHeight) -> {
				double newHeight = targetHeight.doubleValue();
				double newWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				resize(newWidth, newHeight);
			});
		} else {
			getSubSceneFX().widthProperty().bind(parentScene.widthProperty());
			getSubSceneFX().heightProperty().bind(parentScene.heightProperty());
		}
	}
}