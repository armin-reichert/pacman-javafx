/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.scene;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
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
	default GameModel game() {
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