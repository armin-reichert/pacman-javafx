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

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;

/**
 * Common interface of all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	/**
	 * Sets the scene "context" (game model, rendering, 3D model, sound).
	 */
	void setGame(GameModel game);

	/**
	 * Called before scene is displayed.
	 */
	default void init() {
		log("Scene '%s' init", getClass().getName());
	}

	/**
	 * Called on every tick.
	 */
	void update();

	/**
	 * Called before scene ends.
	 */
	default void end() {
		log("Scene '%s' end", getClass().getName());
	}

	/**
	 * @return the JavaFX subscene associated with this game scene
	 */
	SubScene getFXSubScene();

	/**
	 * Resizes the associated FX subscene to the given height.
	 * 
	 * @param height height in pixels
	 */
	void resize(double height);

	/**
	 * @return if this is a 3D scene
	 */
	boolean is3D();

	default void onKeyPressed(KeyCode key) {
	}
}