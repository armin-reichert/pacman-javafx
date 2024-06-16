/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import javafx.scene.canvas.Canvas;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_PIP_OPACITY_PERCENT;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView extends Canvas {

    private final PlayScene2D displayedScene = new PlayScene2D();

    public PictureInPictureView(GameContext context) {
        displayedScene.setContext(context);
        displayedScene.setCanvas(this);
        displayedScene.scalingPy.bind(heightProperty().divide(GameModel.ARCADE_MAP_SIZE_Y));
        widthProperty().bind(heightProperty().multiply(0.777));
        opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
    }

    public void draw() {
        if (isVisible()) {
            displayedScene.draw();
        }
    }
}
