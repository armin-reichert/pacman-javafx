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
package de.amr.games.pacman.ui.fx.shell;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;

/**
 * Picture-In-Picture view. Displays an embedded 2D play scene.
 * 
 * @author Armin Reichert
 */
public class PiPView extends Pane {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public final DoubleProperty heightPy = new SimpleDoubleProperty() {
		@Override
		protected void invalidated() {
			playScene.resizeToHeight((float) get());
		}
	};

	private Vector2f minSize;
	private Vector2f maxSize;
	private final PlayScene2D playScene;

	public PiPView(Vector2f minSize, float maxZoom) {
		this.minSize = Objects.requireNonNull(minSize);
		maxSize = minSize.scaled(maxZoom);
		playScene = new PlayScene2D();
		playScene.resizeToHeight(minSize.y());
		getChildren().add(playScene.fxSubScene());
		setFocusTraversable(false);
	}

	public Vector2f minSize() {
		return minSize;
	}

	public Vector2f maxSize() {
		return maxSize;
	}

	public void setContext(GameSceneContext context) {
		LOGGER.trace("Initialize PiP view");
		playScene.setContext(context);
	}

	public void update() {
		LOGGER.trace("Update PiP view");
		playScene.clear();
		playScene.draw();
		playScene.drawHUD();
	}
}