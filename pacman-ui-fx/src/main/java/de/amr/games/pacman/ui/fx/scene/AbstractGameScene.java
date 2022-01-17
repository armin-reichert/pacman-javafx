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

import static de.amr.games.pacman.lib.Logging.log;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.util.AbstractCameraController;
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

	protected final PacManGameUI ui;
	protected PacManGameController gameController;
	protected GameModel game;

	public AbstractGameScene(PacManGameUI ui) {
		this.ui = ui;
	}

	/**
	 * @return the JavaFX subscene
	 */
	public abstract SubScene getSubSceneFX();

	/**
	 * Tells if this is a 3D scene.
	 * 
	 * @return {@code true} if this is a 3D scene
	 */
	public abstract boolean is3D();

	/**
	 * @return for 3D scenes, the current camera controller, for 2D scenes returns {@code null}
	 */
	public abstract AbstractCameraController currentCameraController();

	/**
	 * Called when the scene gets initialized. Stores a reference to the game controller and the current game such that
	 * the other lifecycle methods can use them.
	 */
	public void init(PacManGameController gameController) {
		this.gameController = gameController;
		this.game = gameController.game();
	}

	/**
	 * Called on every tick.
	 */
	public abstract void update();

	/**
	 * Forces the timer of the current state to expire.
	 */
	public void continueGame() {
		gameController.stateTimer().expire();
	}

	/**
	 * Called when the scene ends.
	 */
	public void end() {
		log("End scene '%s'", getClass().getSimpleName());
	}

	/**
	 * @return aspect ratio for this scene
	 */
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	/**
	 * Resizes the scene to the given size.
	 * 
	 * @param width  with in pixels
	 * @param height height in pixels
	 */
	public void resize(double width, double height) {
	}

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