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

import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.lib.Logging;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D). Every implementation of this interface has a JavaFX sub scene which
 * is a child of the main scene inside the stage.
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	void createFXSubScene(Scene parentScene);

	SubScene getSubSceneFX();

	void init();

	void onGameEvent(GameEvent event);

	void update();

	boolean is3D();

	default void end() {
		Logging.log("Scene '%s' ended", name());
	}

	default String name() {
		return getClass().getSimpleName();
	}
}