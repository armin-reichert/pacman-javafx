/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.canvas.Canvas;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView extends Canvas {

    private final GameScene2D gameScene;

    public PictureInPictureView(GameScene2D gameScene2D, GameContext context) {
        this.gameScene = checkNotNull(gameScene2D);
        gameScene.setContext(context);
        gameScene.setCanvas(this);
        gameScene.scalingPy.bind(heightProperty().divide(GameModel.ARCADE_MAP_SIZE_Y));
        widthProperty().bind(heightProperty().multiply(0.777));
    }

    public void draw() {
        if (isVisible()) {
            gameScene.draw();
        }
    }
}
