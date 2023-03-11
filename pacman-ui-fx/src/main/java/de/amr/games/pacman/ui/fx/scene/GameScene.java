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
import de.amr.games.pacman.lib.timer.TickTimer;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Interface implemented by all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	/**
	 * @return the scene context (game controller, model, rendering, 3D model, sound)
	 */
	GameSceneContext context();

	/**
	 * Called when the scene becomes the current one.
	 */
	default void init() {
		// empty default
	}

	void update();

	void draw();

	/**
	 * Called when the scene is replaced by another one.
	 */
	default void end() {
		// empty default
	}

	/**
	 * @return the JavaFX subscene associated with this game scene
	 */
	SubScene fxSubScene();

	/**
	 * Called when this game scene is embedded (displayed) inside its parent FX scene.
	 * 
	 * @param parentScene the JavaFX parent scene
	 */
	void onEmbed(Scene parentScene);

	/**
	 * Called when the height of the parent of this game scene has changed.
	 * 
	 * @param new height height in pixels
	 */
	void resizeToHeight(double height);

	/**
	 * @return if this is a scene with 3D content
	 */
	boolean is3D();

	/**
	 * Called when scene variants for 2D and 3D exist and variant changes from 2D to 3D.
	 */
	default void onSwitchFrom2D() {
		// empty default
	}

	/**
	 * Called when scene variants for 2D and 3D exist and variant changes from 3D to 2D.
	 */
	default void onSwitchFrom3D() {
		// empty default
	}

	/**
	 * Handles keyboard input.
	 */
	default void handleKeyboardInput() {
		// empty default
	}

	/**
	 * "Locks" the current game controller state by setting the timer duration to {@link TickTimer#INDEFINITE}.
	 */
	default void lockGameState() {
		context().state().timer().resetIndefinitely();
	}

	/**
	 * "Unlocks" the current game controller state by forcing the timer to expire.
	 */
	default void unlockGameState() {
		context().state().timer().expire();
	}
}