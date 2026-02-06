/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class TotalPerspective implements Perspective<GameLevel> {

    private final PerspectiveCamera camera;

    public TotalPerspective(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
    }

    @Override
    public void startControlling() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(70);
    }

    @Override
    public void update(GameLevel level) {
        int sizeX = level.worldMap().numCols() * TS;
        int sizeY = level.worldMap().numRows() * TS;
        camera.setTranslateX(sizeX * 0.5);
        camera.setTranslateY(sizeY * 1.5);
        camera.setTranslateZ(-100);
    }
}
