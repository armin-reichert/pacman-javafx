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
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common base class for all game scenes (2D and 3D).
 * 
 * <p>
 * Each game scene has an associated JavaFX subscene.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene implements DefaultPacManGameEventHandler {

	protected PacManGameController gameController;

	/**
	 * @return the JavaFX subscene
	 */
	public abstract SubScene getSubSceneFX();

	/**
	 * Called when the scene gets initialized. Stores a reference to the game controller such that the other lifecycle
	 * methods have access to it.
	 */
	public void init(PacManGameController gameController) {
		this.gameController = gameController;
	}

	/**
	 * Called on every tick.
	 */
	public abstract void update();

	/**
	 * Called when the scene ends.
	 */
	public abstract void end();

	/**
	 * @return aspect ratio for this scene
	 */
	public abstract OptionalDouble aspectRatio();

	/**
	 * Resizes the scene to the given size.
	 * 
	 * @param width  with in pixels
	 * @param height height in pixels
	 */
	public abstract void resize(double width, double height);

	/**
	 * Keeps the scene size at the size of the parent scene.
	 * 
	 * @param parentScene the parent scene (main scene)
	 */
	public void keepSizeOf(Scene parentScene) {
		if (aspectRatio().isPresent()) {
			double aspectRatio = aspectRatio().getAsDouble();
			parentScene.widthProperty().addListener(($1, $2, targetWidth) -> {
				double newHeight = Math.min(targetWidth.doubleValue() / aspectRatio, parentScene.getHeight());
				double newWidth = newHeight * aspectRatio;
				resize(newWidth, newHeight);
			});
			parentScene.heightProperty().addListener(($1, $2, targetHeight) -> {
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