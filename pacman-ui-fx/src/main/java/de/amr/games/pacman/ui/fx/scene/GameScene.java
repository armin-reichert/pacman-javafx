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

import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.SubScene;

/**
 * Interface implemented by all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener {

	/**
	 * Called before scene is displayed.
	 */
	void init();

	/**
	 * Called on every tick.
	 */
	void update();

	/**
	 * Called before scene ends.
	 */
	void end();

	/**
	 * Sets the context for the current scene.
	 * 
	 * @param game the game model
	 * @param r2D  the 2D rendering
	 */
	void setContext(GameModel game, Rendering2D r2D);

	/**
	 * @return the JavaFX subscene associated with this game scene
	 */
	SubScene getFXSubScene();

	/**
	 * Resizes the FX scene to fit into the given height.
	 * 
	 * @param height height in pixels
	 */
	void resizeFXSubScene(double height);

	/**
	 * @return if this is a 3D scene
	 */
	boolean is3D();
}