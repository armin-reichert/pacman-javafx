/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;

public class TotalPerspective implements Perspective {

    @Override
    public void init(PerspectiveCamera camera) {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(70);
    }

    @Override
    public void update(PerspectiveCamera camera, GameContext gameContext) {
        gameContext.optGameLevel().ifPresent(gameLevel -> {
            int sizeX = gameLevel.worldMap().numCols() * TS;
            int sizeY = gameLevel.worldMap().numRows() * TS;
            camera.setTranslateX(sizeX * 0.5);
            camera.setTranslateY(sizeY * 1.5);
            camera.setTranslateZ(-100);
        });
    }
}
