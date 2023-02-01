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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.scene.layout.Pane;

/**
 * Picture-In-Picture view. Displays an embedded 2D play scene.
 * 
 * @author Armin Reichert
 */
public class PiPView extends Pane {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final PlayScene2D playScene;

	public PiPView(float height) {
		playScene = new PlayScene2D();
		playScene.resizeToHeight(height);
		getChildren().add(playScene.fxSubScene());
		setFocusTraversable(false);
	}

	public void setPlaySceneHeight(double height) {
		playScene.resizeToHeight(height);
	}

	public void setContext(GameSceneContext context) {
		LOG.trace("Initialize PiP view");
		playScene.setContext(context);
	}

	public void update() {
		if (isVisible()) {
			LOG.trace("Update PiP view");
			playScene.clear();
			playScene.drawSceneContent();
			playScene.drawHUD();
		}
	}
}