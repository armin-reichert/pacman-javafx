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
public class PictureInPicture extends PlayScene2D {

    private final Canvas canvas;

    public final DoubleProperty heightPy = new SimpleDoubleProperty(this, "height", PIP_MIN_HEIGHT) {
        @Override
        protected void invalidated() {
            double scaling = get() / CANVAS_HEIGHT_UNSCALED;
            canvas.setWidth(CANVAS_WIDTH_UNSCALED * scaling);
            canvas.setHeight(CANVAS_HEIGHT_UNSCALED * scaling);
            setScaling(scaling);
        }
    };

    public final DoubleProperty opacityPy = new SimpleDoubleProperty(this, "opacity", 1.0);

    public PictureInPicture(GameSceneContext sceneContext) {
        double height = heightPy.doubleValue();
        double aspectRatio = (double) CANVAS_WIDTH_UNSCALED / CANVAS_HEIGHT_UNSCALED;
        canvas = new Canvas(height * aspectRatio, height);
        canvas.opacityProperty().bind(opacityPy);
        setScoreVisible(true);
        setContext(sceneContext);
        setCanvas(canvas);
        clearCanvas();
    }
}