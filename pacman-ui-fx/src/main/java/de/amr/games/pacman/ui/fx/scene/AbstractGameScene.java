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

import java.util.Optional;
import java.util.OptionalDouble;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DCameraController;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common base class for all game scenes (2D and 3D).
 * 
 * <p>
 * Each game scene has an associated JavaFX subscene of the main scene.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene extends DefaultGameEventHandler {

	protected final PacManGameUI_JavaFX ui;
	protected final GameController gameController;

	protected SubScene fxSubScene;
	protected GameModel game;

	public AbstractGameScene(PacManGameUI_JavaFX ui) {
		this.ui = ui;
		this.gameController = ui.gameController;
	}

	public String name() {
		return getClass().getSimpleName();
	}

	/**
	 * Tells if this is a 3D scene.
	 * 
	 * @return {@code true} if this is a 3D scene
	 */
	public abstract boolean is3D();

	/**
	 * @return The current camera controller in case of a 3D scene, else {@code Optional.empty()}.
	 */
	public abstract Optional<PlayScene3DCameraController> camController();

	/**
	 * Called when the scene gets initialized.
	 * 
	 * <p>
	 * Stores a reference to the current game model such that the other lifecycle methods can use it.
	 */
	public void init(Scene parentScene) {
		this.game = gameController.game;
		createFXSubScene(parentScene);
	}

	/**
	 * Creates the JavaFX subscene associated with this game scene
	 * 
	 * @param parentScene the main scene
	 */
	protected abstract void createFXSubScene(Scene parentScene);

	/**
	 * @return the JavaFX subscene
	 */
	public SubScene getSubSceneFX() {
		return fxSubScene;
	}

	/**
	 * Called on every tick.
	 */
	public abstract void update();

	/**
	 * Forces the timer of the game controller's current state to expire.
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
}