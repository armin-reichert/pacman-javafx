/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.CANVAS_HEIGHT_UNSCALED;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.CANVAS_WIDTH_UNSCALED;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PIP_MIN_HEIGHT;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(this, "height", PIP_MIN_HEIGHT) {
		@Override
		protected void invalidated() {
			double scaling = get() / CANVAS_HEIGHT_UNSCALED;
			canvas.setWidth(CANVAS_WIDTH_UNSCALED * scaling);
			canvas.setHeight(CANVAS_HEIGHT_UNSCALED * scaling);
			playScene2D.setScaling(scaling);
		}
	};

	public final DoubleProperty opacityPy = new SimpleDoubleProperty(this, "opacity", 1.0);

	private final Canvas canvas;
	private final PlayScene2D playScene2D;

	public PictureInPicture(GameSceneContext sceneContext) {
		double h = heightPy.doubleValue();
		double aspectRatio = (double) CANVAS_WIDTH_UNSCALED / CANVAS_HEIGHT_UNSCALED;
		canvas = new Canvas(h * aspectRatio, h);
		canvas.opacityProperty().bind(opacityPy);
		playScene2D = new PlayScene2D();
		playScene2D.setCanvas(canvas);
		playScene2D.setScoreVisible(true);
		playScene2D.setContext(sceneContext);
	}

	public Canvas canvas() {
		return canvas;
	}

	public void draw() {
		playScene2D.draw();
	}
}