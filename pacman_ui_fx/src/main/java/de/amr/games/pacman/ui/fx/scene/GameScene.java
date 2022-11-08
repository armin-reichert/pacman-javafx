/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Interface implemented by all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventAdapter {

	/** Default unscaled scene size in pixels: number of tiles times tile size (8). */
	public static final V2i DEFAULT_SIZE = ArcadeWorld.WORLD_SIZE;

	/**
	 * Sets the scene context (game controller/model, 2D rendering, 3D model, sound).
	 * <p>
	 * This method is called before the scene's init method.
	 */
	void setContext(SceneContext context);

	/**
	 * @return the scene context (game controller, model, rendering, 3D model, sound)
	 */
	SceneContext ctx();

	/**
	 * Called when the scene becomes the current one.
	 */
	default void init() {
		// empty default
	}

	/**
	 * Called on every tick.
	 */
	void onTick();

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
	void embedInto(Scene parentScene);

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
	 * Called when a key has been pressed. The keyboard state can be queried using the {@link Keyboard} class.
	 */
	default void onKeyPressed() {
	}

	/**
	 * "Locks" the current game controller state by setting the timer duration to {@link TickTimer#INDEFINITE}.
	 */
	default void lockGameState() {
		ctx().state().timer().resetIndefinitely();
	}

	/**
	 * "Unlocks" the current game controller state by forcing the timer to expire.
	 */
	default void unlockGameState() {
		ctx().state().timer().expire();
	}
}