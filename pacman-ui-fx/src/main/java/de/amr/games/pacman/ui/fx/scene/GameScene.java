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

import de.amr.games.pacman.event.GameEventListener;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Interface implemented by all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	/**
	 * @return the scene context (game controller, game model, game level, rendering, sounds).
	 */
	GameSceneContext context();

	/**
	 * Called when the scene becomes the current one.
	 */
	default void init() {
		// empty default
	}

	/**
	 * Called when the scene needs to be updated.
	 */
	void update();

	/**
	 * Called when the scene needs to be rendered.
	 */
	void render();

	/**
	 * Called when the scene ends and gets replaced by another scene.
	 */
	default void end() {
		// empty default
	}

	/**
	 * @return the JavaFX subscene associated with this game scene
	 */
	SubScene fxSubScene();

	/**
	 * @return if this is a scene with 3D content
	 */
	boolean is3D();

	/**
	 * Called when this game scene is embedded (displayed) inside its parent FX scene.
	 * 
	 * @param parentScene the JavaFX parent scene
	 */
	void onEmbedIntoParentScene(Scene parentScene);

	/**
	 * Called when the size of the parent of this game scene changes.
	 * 
	 * @param parentScene the JavaFX parent scene
	 */
	void onParentSceneResize(Scene parentScene);

	/**
	 * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
	 */
	default void onSceneVariantSwitch() {
		// empty default
	}

	/**
	 * Handles keyboard input.
	 */
	default void handleKeyboardInput() {
		// empty default
	}

	/**
	 * "Locks" the current game state by setting an indefinite timer duration.
	 */
	default void lockGameState() {
		context().state().timer().resetIndefinitely();
	}

	/**
	 * "Unlocks" the current game state by forcing the timer to expire.
	 */
	default void unlockGameState() {
		context().state().timer().expire();
	}
}