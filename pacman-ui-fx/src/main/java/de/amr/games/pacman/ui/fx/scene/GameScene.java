/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEventListener;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Base class of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public abstract class GameScene implements GameEventListener {

	protected final GameSceneContext context;
	protected SubScene fxSubScene;

	protected GameScene(GameController gameController) {
		context = new GameSceneContext(gameController);
	}

	/**
	 * @return the scene context (game controller, game model, game level, rendering, sounds).
	 */
	public GameSceneContext context() {
		return context;
	}

	/**
	 * Called when the scene becomes the current one.
	 */
	public void init() {
		// empty default
	}

	/**
	 * Called when the scene needs to be updated.
	 */
	public abstract void update();

	/**
	 * Called when the scene needs to be rendered.
	 */
	public void render() {
		// empty default
	}

	/**
	 * Called when the scene ends and gets replaced by another scene.
	 */
	public void end() {
		// empty default
	}

	/**
	 * @return the JavaFX subscene associated with this game scene
	 */
	public SubScene fxSubScene() {
		return fxSubScene;
	}

	/**
	 * @return if this is a scene with 3D content
	 */
	public abstract boolean is3D();

	/**
	 * Called when this game scene is embedded (displayed) inside its parent FX scene.
	 * 
	 * @param parentScene the JavaFX parent scene
	 */
	public abstract void onEmbedIntoParentScene(Scene parentScene);

	/**
	 * Called when the size of the parent of this game scene changes.
	 * 
	 * @param parentScene the JavaFX parent scene
	 */
	public abstract void onParentSceneResize(Scene parentScene);

	/**
	 * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
	 */
	public void onSceneVariantSwitch() {
		// empty default
	}

	/**
	 * Handles keyboard input.
	 */
	public void handleKeyboardInput() {
		// empty default
	}

	/**
	 * "Locks" the current game state by setting an indefinite timer duration.
	 */
	public void lockGameState() {
		context.state().timer().resetIndefinitely();
	}

	/**
	 * "Unlocks" the current game state by forcing the timer to expire.
	 */
	public void unlockGameState() {
		context.state().timer().expire();
	}
}