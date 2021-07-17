package de.amr.games.pacman.ui.fx.scenes.common;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.model.common.PacManGameModel;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D).
 * 
 * <p>
 * Each game scene has an associated JavaFX subscene.
 * 
 * @author Armin Reichert
 */
public interface GameScene extends DefaultPacManGameEventHandler {

	/**
	 * @return the JavaFX subscene
	 */
	SubScene getSubSceneFX();

	/**
	 * Called before the scene is started.
	 */
	void init();

	/**
	 * Called on every tick.
	 */
	void update();

	/**
	 * Called before the scene is terminated.
	 */
	void end();

	/**
	 * @return the game controller
	 */
	PacManGameController getGameController();

	/**
	 * Sets the game controller
	 * 
	 * @param gameController the game controller
	 */
	void setGameController(PacManGameController gameController);

	/**
	 * @return the game model or {@code null}
	 */
	default PacManGameModel game() {
		return getGameController() != null ? getGameController().game() : null;
	}

	/**
	 * @return aspect ratio defined for this scene
	 */
	OptionalDouble aspectRatio();

	/**
	 * Resizes the scene to the given size
	 * 
	 * @param width  with in pixels
	 * @param height height in pixels
	 */
	void resize(double width, double height);

	/**
	 * Keeps the scene size to the size of the parent scene
	 * 
	 * @param parentScene the parent scene (main scene)
	 */
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