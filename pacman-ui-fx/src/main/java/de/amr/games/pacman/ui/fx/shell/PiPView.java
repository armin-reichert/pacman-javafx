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

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Picture-In-Picture view. Displays an embedded 2D game scene.
 * 
 * @author Armin Reichert
 */
public class PiPView extends StackPane {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final Vector2f MIN_SIZE = ArcadeWorld.SIZE_PX.toFloatVec();
	public static final Vector2f MAX_SIZE = ArcadeWorld.SIZE_PX.toFloatVec().scaled(2.0f);

	public final DoubleProperty heightPy = new SimpleDoubleProperty() {
		@Override
		protected void invalidated() {
			playScene.resizeToHeight((float) get());
		}
	};

	private final PlayScene2D playScene = new PlayScene2D();

	public PiPView() {
		heightPy.bind(Env.pipSceneHeightPy);
		opacityProperty().bind(Env.pipOpacityPy);
		setBackground(Ufx.colorBackground(Color.BLACK));
		setFocusTraversable(false);
		getChildren().add(playScene.fxSubScene());
		playScene.resizeToHeight(MIN_SIZE.y());
	}

	public void init(SceneContext context) {
		LOGGER.trace("Initialize PiP view");
		playScene.setContext(context);
//		playScene.init();
	}

	public void update() {
		if (isVisible()) {
			LOGGER.trace("Update PiP view");
			playScene.clear();
			playScene.draw();
			playScene.drawHUD();
		}
	}
}